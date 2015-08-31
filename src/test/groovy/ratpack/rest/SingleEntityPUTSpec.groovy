package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.Car
import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec

import static org.apache.http.HttpStatus.*

class SingleEntityPUTSpec extends RestDslSpec implements JsonHelper {

    def "can update an existing entity"() {
        given:
            app ([entity(name, [data])])

        when:
            jsonPayload newData
            put "${path(name)}/${data.id}"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "${path(name)}/${data.id}"

        then:
            !json.array
            data.id        == json.id.asText()
            newData.field1 == json.field1.asText()

        where:
            name    = newEntityName()
            data    = [id: newId(), field1: 'value']
            newData = [field1: 'newValue']
    }

    def "updating an existing with no data returns not modified"() {
        given:
            app ([entity(name, [data])])

        when:
            put "${path(name)}/${data.id}"

        then:
            response.statusCode == SC_NOT_MODIFIED

        when:
            get "${path(name)}/${data.id}"

        then:
            !json.array
            data.id     == json.id.asText()
            data.field1 == json.field1.asText()

        where:
            name = newEntityName()
            data = [id: newId(), field1: 'value']
    }

    def "updating an existing entity with arbitrary json data leaves previous data intact"() {
        given:
            app ([entity(name, [data])])

        when:
            jsonPayload newData
            put "${path(name)}/${data.id}"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "${path(name)}/${data.id}"

        then:
            !json.array
            data.id        == json.id.asText()
            data.field1    == json.field1.asText()
            newData.field2 == json.field2.asText()

        where:
            name = newEntityName()
            data = [id: newId(), field1: 'value']
            newData = [field2: 'value2']
    }

    def "can update an existing typed entity"() {
        given:
            app ([entity(Bus, [bus])])

        when:
            jsonPayload data
            put "${path(name)}/$bus.id"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "${path(name)}/${bus.id}"

        then:
            !json.array
            bus.id      == json.id.asText()
            data.name   == json.name.asText()
            data.colour == json.colour.asText()

        where:
            name = 'bus'
            bus  = new Bus(id: newId())
            data = [name:'419', colour:'red']
    }

    def "can update a validating entity"() {
        given:
            app ([entity(Car, [car])])

        when:
            jsonPayload data
            put "${path(name)}/${car.id}"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "${path(name)}/${car.id}"

        then:
            !json.array
            car.id == json.id.asText()

            data.manufacturer == json.manufacturer.asText()
            data.colour       == json.colour.asText()

        where:
            name = 'car'
            car  = new Car(id: newId(), manufacturer: 'Renault')
            data = [manufacturer:'Volvo', colour:'red']
    }

    def "cannot update a typed entity with unknown fields"() {
        given:
            app ([entity(Bus, [bus])])

        when:
            jsonPayload data
            put "${path(name)}/${bus.id}"

        then:
            def errors = getJson(SC_BAD_REQUEST)

        and:
            'hairstyle'                      == errors[0].field.asText()
            name                             == errors[0].type.asText()
            'Unrecognized field "hairstyle"' == errors[0].message.asText()
            !errors[0].value

//  Implementing multiple error reporting is not straightforward as the JacksonModule declares ObjectMappers as a singleton,
//  making the addListener() approach non-request/thread safe. Currently we just catch the first exception and return that.
//  Also the errors are returned in order of encounter, hence 'hairstyle' is before 'fish'.
//
//        and:
//            'fish'          == errors[0].field.asText()
//            name            == errors[0].type.asText()
//            'unknown field' == errors[0].message.asText()
//            data.fish       == errors[0].value.asText()

        where:
            name = 'bus'
            bus  = new Bus(id: newId(), colour: 'blue')
            data = [hairstyle:'red', fish:'hake']
    }

    def "cannot add an entry to an unknown entity type"() {
        given:
            app()

        when:
            jsonPayload data
            put "${path(name)}/$id"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
            id   = newId()
            data = [field: 'value']
    }

    def "cannot update an unknown entity"() {
        given:
            app([entity(name, [])])

        when:
            jsonPayload data
            put "${path(name)}/$id"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
            id   = newId()
            data = [field: 'value']
    }

    def 'must supply an id in the url'() {
        given:
            app ([entity(name, [])])

        when:
            put "${path(name)}"

        then:
            def errors = getJson(SC_BAD_REQUEST)
            'id'    == errors[0].field.asText()
            name    == errors[0].type.asText()
            'must be specified in url by client' == errors[0].message.asText()
            !errors[0].value

        where:
            name = newEntityName()
    }

    def "updating a validating entity with invalid data returns a 400 bad request with error details"() {
        given:
            app ([entity(Car, [car])])

        when:
            jsonPayload data
            put "${path(name)}/${car.id}"

        then:
            def errors = getJson(SC_BAD_REQUEST)
            1 == errors.size()
            'manufacturer'    == errors[0].field.asText()
            'Car'             == errors[0].type.asText()
            'may not be null' == errors[0].message.asText()
            !errors[0].value

        where:
            name = 'car'
            car  = new Car(id: newId(), manufacturer: 'Volvo')
            data = [manufacturer: null, colour:'red']
    }
//
//    def "can update existing entities"() { //IS THIS A POST OR PUT? Maybe POST SHOULD FAIL IF THERE ARE EXISTING?
//        // AND PUT SHOULD FAIL IF ANY OF THE ENTITES BEING UPDATED DO NOT EXIST?
//        given:
//            app ([entity(name, [])])
//
//        when:
//            post "/api/$name"
//
//        then:
//            201 == response.statusCode
//            response.headers['location']
//
//        when:
//            String id = idFromResponse
//            get response.headers['location']
//
//        then:
//            !json.array
//            id == json.id.asText()
//
//        when:
//            get "/api/$name"
//
//        then:
//            json.array
//            1  == json.size()
//            id == json[0].id.asText()
//
//        where:
//            name = newEntityName()
//    }

}
