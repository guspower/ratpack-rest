package ratpack.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import ratpack.http.client.RequestSpec
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore
import ratpack.test.internal.RatpackGroovyDslSpec
import spock.lang.Shared
import spock.lang.Unroll

import javax.validation.constraints.NotNull

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

    def "can add a new entity with arbitrary json data"() {
        given:
            app ([entity(name)])

        when:
            jsonPayload data
            post "/api/$name"

        then:
            201 == response.statusCode
            response.headers['location']

        when:
            String id = idFromResponse
            get response.headers['location']

        then:
            !json.array
            data.field1 == json.field1.asText()
            data.field2 == json.field2.asText()

        where:
            name = entityName()
            data = [field1:'data1', field2:'data2']
    }

    def "can add a new typed entity and retrieve it"() {
        given:
            app ([entity(Bus)])

        when:
            jsonPayload data
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

            data.name   == json.name.asText()
            data.colour == json.colour.asText()

        where:
            name = 'bus'
            data = [name:'419', colour:'red']
    }

    def "can add a valid validating entity"() {
        given:
            app ([entity(Car)])

        when:
            jsonPayload data
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

            data.manufacturer == json.manufacturer.asText()
            data.colour       == json.colour.asText()

        where:
            name = 'car'
            data = [manufacturer:'Volvo', colour:'red']
    }

    def "invalid validating entity returns a 400 bad request with error details"() {
        given:
            app ([entity(Car)])

        when:
            jsonPayload data
            post "/api/$name"

        then:
            400 == response.statusCode

        where:
            name = 'car'
            data = [colour:'red']
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

    private EntityStore store(Class type = HashMap.class, List data = []) {
        data ? new InMemoryEntityStore(type, data) : new InMemoryEntityStore(type)
    }

    private RestEntity entity(String name, List data = []) {
        new RestEntity(name, store(HashMap.class, data))
    }

    private RestEntity entity(Class type, List data = []) {
        new RestEntity(type, store(type, data))
    }

    private String entityName() {
        "entity-${UUID.randomUUID()}"
    }

    private String getIdFromResponse() {
        response.headers['location'].tokenize('/')[-1]
    }

    private void jsonPayload(Map data) {
        requestSpec { RequestSpec spec ->
            spec.body { RequestSpec.Body body ->
                body.type('application/json')
                body.text(JsonOutput.toJson(data))
            }
        }
    }

}

@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Bus {

    String id
    String name
    String colour

}

@EqualsAndHashCode
@ToString(includePackage = false, includeNames = true)
class Car {

    String id

    @NotNull
    String manufacturer

    String colour

}
