package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.JsonHelper
import ratpack.rest.fixture.RestDslSpec

import static org.apache.http.HttpStatus.SC_CREATED

class DualEntityRelationshipSpec extends RestDslSpec implements JsonHelper {

    def 'can relate one entity to another of the same type'() {
        given:
            app ([entity(entityName, entityType, data)])

        when:
            jsonPayload ([from: data[0].id, to: data[1].id, name: relationshipName])
            post "/api/relation"

        then:
            response.statusCode == SC_CREATED
            response.headers['location']

        when:
            String id = idFromResponse
            get response.headers['location']

        then:
            !json.array
            id         == json.id.asText()
//            data[0].id == json.from.asText()
//            data[1].id == json.to.asText()
//            relationshipName == json.name.asText()

        where:
            entityName       = newEntityName()
            entityType       = Bus
            relationshipName = 'fleet'
            data = [new Bus(id: newId()), new Bus(id: newId())]
    }

    def 'can relate one entity to another of a different type'() {}
    def 'cannot relate an entity to one that does not exist'() {}
    def 'cannot relate an entity to a type that does not exist'() {}
    def 'relationship request with missing data fails'() {}
    def 'relationship request with invalid relationship name fails'() {}
    def 'can get details about a relationship'() {}
    def 'can get details about a relationship with entity details'() {}
    def 'can update a relationship'() {}
    def 'can delete a relationship'() {}

}
