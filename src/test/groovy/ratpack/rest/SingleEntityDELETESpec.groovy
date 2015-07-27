package ratpack.rest

import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec

import static org.apache.http.HttpStatus.*

class SingleEntityDELETESpec extends RestDslSpec implements JsonHelper {

    def "delete single entity"() {
        given:
            app ([entity(name, [[id:id]])])

        when:
            get "/api/$name/$id"

        then:
            id == json.id.asText()

        when:
            delete "/api/$name/$id"

        then:
            response.statusCode == SC_NO_CONTENT

        when:
            get "/api/$name/$id"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
            id   = newId()
    }

    def "returns 404 for delete unknown entity"() {
        given:
            app ([entity(name, [])])

        when:
            delete "/api/$name/$id"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            name = newEntityName()
            id   = newId()
    }

    def "delete all entities"() {
        given:
            app ([entity(name, data)])

        when:
            get "/api/$name"

        then:
            json.array
            2 == json.size()

        when:
            delete "/api/$name"

        then:
            response.statusCode == SC_NO_CONTENT

        when:
            get "/api/$name"

        then:
            json.array
            0 == json.size()

        where:
            data = [[field:'value1'], [field:'value2']]
            name = newEntityName()
    }

    def "returns 404 for delete unknown entity type"() {
        given:
            app ([])

        when:
            delete "/api/$unknown"

        then:
            response.statusCode == SC_NOT_FOUND

        where:
            unknown = newEntityName()
    }

}
