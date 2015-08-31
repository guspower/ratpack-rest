package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.Car
import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec

import static org.apache.http.HttpStatus.*

class SingleEntityPOSTSpec extends RestDslSpec implements JsonHelper {

    def "can add a new entity and retrieve it"() {
        given:
            app ([entity(name, [])])

        when:
            post "${path(name)}"

        then:
            response.statusCode == SC_CREATED
            response.headers['location']

        when:
            String id = idFromResponse
            get response.headers['location']

        then:
            !json.array
            id == json.id.asText()

        when:
            get "${path(name)}"

        then:
            json.array
            1  == json.size()
            id == json[0].id.asText()

        where:
            name = newEntityName()
    }

    def "can add a new entity with arbitrary json data"() {
        given:
            app ([entity(name, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            response.statusCode == SC_CREATED
            response.headers['location']

        when:
            String id = idFromResponse
            get response.headers['location']

        then:
            !json.array
            data.field1 == json.field1.asText()
            data.field2 == json.field2.asText()

        where:
            name = newEntityName()
            data = [field1:'data1', field2:'data2']
    }

    def "cannot add a new entity with an id supplied in the url"() {
        given:
            app ([entity(name, [])])

        when:
            post "${path(name)}/$id"

        then:
            def errors = getJson(SC_BAD_REQUEST)
            'id'    == errors[0].field.asText()
            name    == errors[0].type.asText()
            'cannot be specified by client' == errors[0].message.asText()
            id      == errors[0].value.asText()

        where:
            name = newEntityName()
            id   = newId()
    }

    def "cannot add a new entity with an id supplied in the payload"() {
        given:
            app ([entity(name, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            def errors = getJson(SC_BAD_REQUEST)
            'id'    == errors[0].field.asText()
            name    == errors[0].type.asText()
            'cannot be specified by client' == errors[0].message.asText()
            data.id == errors[0].value.asText()

        where:
            name = newEntityName()
            data = [id: newId(), field: 'value']
    }

    def "can add a new typed entity and retrieve it"() {
        given:
            app ([entity(Bus, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            response.statusCode == SC_CREATED
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

    def "cannot add a new typed entity with unknown fields"() {
        given:
            app ([entity(Bus, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            def errors = getJson(SC_BAD_REQUEST)

        and:
            'hairstyle'                      == errors[0].field.asText()
            name                             == errors[0].type.asText()
            'Unrecognized field "hairstyle"' == errors[0].message.asText()
            !errors[0].value

//  Implementing multiple error reporting is not straightforward as the JacksonModule declares ObjectMappers as a singleton,
//  making the addListener() approach non-request/thread safe. Currently we just catch the first exception and return that.
//
//        and:
//            'fish'          == errors[0].field.asText()
//            name            == errors[0].type.asText()
//            'unknown field' == errors[0].message.asText()
//            data.fish       == errors[0].value.asText()

        where:
            name = 'bus'
            data = [hairstyle:'red', fish:'hake']
    }

    def "can add a valid validating entity"() {
        given:
            app ([entity(Car, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            response.statusCode == SC_CREATED
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
            app ([entity(Car, [])])

        when:
            jsonPayload data
            post "${path(name)}"

        then:
            def errors = getJson(SC_BAD_REQUEST)
            1 == errors.size()
            'manufacturer'    == errors[0].field.asText()
            'Car'             == errors[0].type.asText()
            'may not be null' == errors[0].message.asText()
            !errors[0].value

        where:
            name = 'car'
            data = [colour:'red']
    }

    def "cannot add an entry to an unknown entity type"() {
        given:
            app()

        when:
            post "${path(name)}"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
    }

}
