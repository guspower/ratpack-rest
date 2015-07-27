package ratpack.rest

import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec

import static org.apache.http.HttpStatus.*

class SingleEntityPUTSpec extends RestDslSpec implements JsonHelper {

    def "can update an existing entity"() {
        given:
            app ([entity(name, [data])])

        when:
            jsonPayload newData
            put "/api/$name/${data.id}"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "/api/$name/${data.id}"

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
            put "/api/$name/${data.id}"

        then:
            response.statusCode == SC_NOT_MODIFIED

        when:
            get "/api/$name/${data.id}"

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
            put "/api/$name/${data.id}"

        then:
            response.statusCode == SC_ACCEPTED

        when:
            get "/api/$name/${data.id}"

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
//
//    def "can update an existing typed entity"() {
//        given:
//            app ([entity(Bus, [])])
//
//        when:
//            jsonPayload data
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
//            data.name   == json.name.asText()
//            data.colour == json.colour.asText()
//
//        where:
//            name = 'bus'
//            data = [name:'419', colour:'red']
//    }
//
//    def "can update a validating entity"() {
//        given:
//            app ([entity(Car, [])])
//
//        when:
//            jsonPayload data
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
//            data.manufacturer == json.manufacturer.asText()
//            data.colour       == json.colour.asText()
//
//        where:
//            name = 'car'
//            data = [manufacturer:'Volvo', colour:'red']
//    }
//
//    def "updating a validating entity with invalid data returns a 400 bad request with error details"() {
//        given:
//            app ([entity(Car, [])])
//
//        when:
//            jsonPayload data
//            post "/api/$name"
//
//        then:
//            def errors = getJson(400)
//            1 == errors.size()
//            'manufacturer'    == errors[0].field.asText()
//            'Car'             == errors[0].type.asText()
//            'may not be null' == errors[0].message.asText()
//            !errors[0].value
//
//        where:
//            name = 'car'
//            data = [colour:'red']
//    }
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
