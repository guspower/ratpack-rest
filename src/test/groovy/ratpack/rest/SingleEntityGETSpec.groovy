package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec
import spock.lang.Unroll

import static org.apache.http.HttpStatus.*

class SingleEntityGETSpec extends RestDslSpec implements JsonHelper {

    def "returns empty json list given no entries"() {
        given:
            app ([entity(name, [])])

        when:
            get "${path(name)}"

        then:
            json.array
            0 == json.size()

        where:
            name = newEntityName()
    }

    def "returns 404 for unknown entity"() {
        given:
            app ([])

        when:
            get "${path(unknown)}"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            unknown = newEntityName()
    }

    @Unroll("returns json list given #data entities")
    def "returns json list given valid entities"() {
        given:
            app ([entity(name, data)])

        when:
            get "${path(name)}"

        then:
            data.size() == json.size()
            'value1'    == json[0].field.asText()
            'value2'    == json[1].field.asText()

        where:
            data = [[field:'value1'], [field:'value2']]
            name = newEntityName()
    }

    def "returns 404 for unknown entity id"() {
        given:
            app ([entity(name, [])])

        when:
            get "${path(name)}/$id"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
            id   = 'unknownId'
    }

    def "returns known entity by id"() {
        given:
            app ([entity(name, data)])

        when:
            get "${path(name)}/$id"

        then:
            !json.array
            id       == json.id.asText()
            'value2' == json.field.asText()

        where:
            name = newEntityName()
            data = [[field:'value1', id:'other'], [field:'value2', id:'expected']]
            id   = 'expected'
    }

    def "returns known typed entity by id"() {
        given:
            app ([entity(name, Bus, data)])

        when:
            get "${path(name)}/$id"

        then:
            !json.array
            id             == json.id.asText()
            data[0].name   == json.name.asText()
            data[0].colour == json.colour.asText()

        where:
            name = newEntityName()
            data = [new Bus(name:'419', colour:'red', id:'expected')]
            id   = 'expected'
    }

}
