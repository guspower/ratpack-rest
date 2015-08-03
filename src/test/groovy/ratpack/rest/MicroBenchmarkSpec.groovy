package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.OnDemandApplicationSpec
import ratpack.rest.fixture.RequestGenerator
import ratpack.rest.fixture.RestDslSpec

class MicroBenchmarkSpec extends OnDemandApplicationSpec {

    int testDuration = 5000
    RequestGenerator generator

    def setup() {
        generator = new RequestGenerator(currentTestName(), testDuration)
    }

    def cleanup() {
        generator.report()
    }

    def "GET - untyped entity"() {
        given:
            app([RestDslSpec.entity(name, data)])

        when:
            Thread thread = generator.run("${application.address}api/$name/$id")

        then:
            thread.join()

        where:
            name = RestDslSpec.newEntityName()
            data = [[field:'value1', id:'other'], [field:'value2', id:'expected']]
            id   = 'expected'
    }

    def "GET - typed entity"() {
        given:
            app([RestDslSpec.entity(name, data)])

        when:
            Thread thread = generator.run("${application.address}api/$name/${data[0].id}")

        then:
            thread.join()

        where:
            name = RestDslSpec.newEntityName()
            data = [new Bus(id: RestDslSpec.newId(), colour: 'green', name: 'Fred')]
    }

}
