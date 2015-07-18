package ratpack.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Memoized
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.RestModule.RestEntity
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore
import ratpack.test.internal.RatpackGroovyDslSpec
import spock.lang.Unroll

class SingleEntitySpec extends RatpackGroovyDslSpec {

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

    @Memoized
    private JsonNode getJson() {
        assert response.statusCode == 200

        def jackson = new ObjectMapper()
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

    private EntityStore store(List data) {
        new InMemoryEntityStore(data)
    }

    private RestEntity entity(String name, List data = []) {
        new RestModule.RestEntity(name: name, store: store(data))
    }

    private String entityName() {
        "entity-${UUID.randomUUID()}"
    }

}
