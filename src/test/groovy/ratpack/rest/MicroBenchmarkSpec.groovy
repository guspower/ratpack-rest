package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.EntityFixture
import ratpack.rest.fixture.HttpFixture
import ratpack.rest.fixture.OnDemandApplicationSpec
import ratpack.rest.fixture.RequestRunner
import ratpack.rest.fixture.RestDslSpec
import spock.lang.Shared

class MicroBenchmarkSpec extends OnDemandApplicationSpec {

    @Shared MicroBenchmarkSettings settings = new MicroBenchmarkSettings()

    RequestRunner runner

    def setup() {
        runner = new RequestRunner(currentTestName(), settings.duration)
    }

    def cleanup() {
        runner.benchmark.report()
    }

    def "GET - untyped entity"() {
        given:
            def entityFixture = new EntityFixture<HashMap>(settings.numberOfEntities, type, config)

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<HashMap>("${application.address}api/$name", entityFixture)

        when:
            runner.run(httpFixture)

        then:
            settings.acceptableSuccessRate < runner.benchmark.successRate
            settings.acceptableThroughput  < runner.benchmark.throughput

        where:
            name   = RestDslSpec.newEntityName()
            type   = HashMap
            config = { it.field = "field-${it.id}".toString() }
    }

    def "GET - typed entity"() {
        given:
            def entityFixture = new EntityFixture<Bus>(settings.numberOfEntities, type, { Bus bus ->
                bus.colour = 'red'
                bus.name = bus.id
            })

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<Bus>("${application.address}api/$name", entityFixture)

        when:
            runner.run(httpFixture)

        then:
            settings.acceptableSuccessRate < runner.benchmark.successRate
            settings.acceptableThroughput  < runner.benchmark.throughput

        where:
            type = Bus
            name = RestDslSpec.newEntityName()
            config = { Bus bus -> bus.colour = 'red'; bus.name = bus.id }
    }

    final class MicroBenchmarkSettings {

        int duration = 5000
        int numberOfEntities = 500

        int acceptableSuccessRate = 99
        int acceptableThroughput  = 100

    }

}


