package ratpack.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Memoized
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore
import ratpack.test.internal.RatpackGroovyDslSpec
import spock.lang.Shared
import spock.lang.Unroll

class SingleEntitySpec extends RatpackGroovyDslSpec {

    @Shared
    def jackson = new ObjectMapper()

    def "returns empty json list given no entries"() {
        given:
            app ([entity(name)])

        when:
            get "/api/$name"

        then:
            json.array
            0 == json.size()

        where:
            name = entityName()
    }

    def "returns 404 for unknown entity"() {
        given:
            app ([])

        when:
            get "/api/$unknown"

        then:
            response.statusCode == 404

        where:
            unknown = entityName()
    }

    @Unroll("returns json list given #data entities")
    def "returns json list given valid entities"() {
        given:
            app ([entity(name, data)])

        when:
            get "/api/$name"

        then:
            data.size() == json.size()
            'value1'    == json[0].field.asText()
            'value2'    == json[1].field.asText()

        where:
            data = [[field:'value1'], [field:'value2']]
            name = entityName()
    }

    def "returns 404 for unknown entity id"() {
        given:
            app ([entity(name)])

        when:
            get "/api/$name/$id"

        then:
            response.statusCode == 404

        where:
            name = entityName()
            id   = 'unknownId'
    }

    def "returns known entity by id"() {
        given:
            app ([entity(name, data)])

        when:
            get "/api/$name/$id"

        then:
            !json.array
            id       == json.id.asText()
            'value2' == json.field.asText()

        where:
            name = entityName()
            data = [[field:'value1', id:'other'], [field:'value2', id:'expected']]
            id   = 'expected'
    }

    def "can add a new entity and retrieve it"() {
        given:
            app ([entity(name)])

        when:
            post "/api/$name"

        then:
            201 == response.statusCode
            response.headers['location']

        when:
            String id = idFromResponse
            get response.headers['location']

        then:
            !json.array
            id == json.id.asText()

        when:
            get "/api/$name"

        then:
            json.array
            1  == json.size()
            id == json[0].id.asText()

        where:
            name = entityName()
    }

    private JsonNode getJson() {
        assert response.statusCode == 200

        jackson.readTree response.body.text
    }

    private void app(List<RestEntity> entities = []) {
        bindings {
            module JacksonModule
            module RestModule, { RestModule.Config config ->
                config.entities.addAll entities
            }
        }
        handlers { RestHandlers rest ->
            rest.register delegate
        }
    }

    private EntityStore store(List data = []) {
        data ? new InMemoryEntityStore(data[0].class, data) : new InMemoryEntityStore()
    }

    private RestEntity entity(String name, List data = []) {
        new RestEntity(name: name, store: store(data))
    }

    private String entityName() {
        "entity-${UUID.randomUUID()}"
    }

    private String getIdFromResponse() {
        response.headers['location'].tokenize('/')[-1]
    }

}
