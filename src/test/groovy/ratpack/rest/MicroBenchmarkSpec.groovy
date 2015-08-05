package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.EntityFixture
import ratpack.rest.fixture.HttpFixture
import ratpack.rest.fixture.OnDemandApplicationSpec
import ratpack.rest.fixture.RequestRunner
import ratpack.rest.fixture.RestDslSpec

class MicroBenchmarkSpec extends OnDemandApplicationSpec {

    static final int testDuration = 5000
    static final int numberOfEntities = 500
    RequestRunner runner

    def setup() {
        runner = new RequestRunner(currentTestName(), testDuration)
    }

    def cleanup() {
        runner.benchmark.report()
    }

    def "GET - untyped entity"() {
        given:
            def entityFixture = new EntityFixture<HashMap>(numberOfEntities, type, config)

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<HashMap>("${application.address}api/$name", entityFixture)

        when:
            Thread thread = runner.run(httpFixture)

        then:
            thread.join()

        where:
            name   = RestDslSpec.newEntityName()
            type   = HashMap
            config = { it.field = "field-${it.id}".toString() }
    }

    def "GET - typed entity"() {
        given:
            def entityFixture = new EntityFixture<Bus>(numberOfEntities, type, { Bus bus ->
                bus.colour = 'red'
                bus.name = bus.id
            })

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<Bus>("${application.address}api/$name", entityFixture)

        when:
            Thread thread = runner.run(httpFixture)

        then:
            thread.join()

        where:
            type = Bus
            name = RestDslSpec.newEntityName()
            config = { Bus bus -> bus.colour = 'red'; bus.name = bus.id }
    }

}


