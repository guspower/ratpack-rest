package ratpack.rest

import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.EntityFixture
import ratpack.rest.fixture.HttpFixture
import ratpack.rest.fixture.OnDemandApplicationSpec
import ratpack.rest.fixture.RequestRunner
import ratpack.rest.fixture.RestDslSpec
import spock.lang.Shared

class MicroBenchmarkSpec extends OnDemandApplicationSpec {

    @Shared Config config = new Config()

    RequestRunner runner

    def setup() {
        runner = new RequestRunner(currentTestName(), config.duration)
    }

    def cleanup() {
        runner.benchmark.report()
    }

    def "GET - untyped entity"() {
        given:
            def entityFixture = new EntityFixture<HashMap>(config.numberOfEntities, type, generator)

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<HashMap>("${application.address}api/$name", entityFixture,
                HttpFixture.Method.GET)

        when:
            runner.run(httpFixture)

        then:
            config.acceptableSuccessRate < runner.benchmark.successRate
            config.acceptableThroughput  < runner.benchmark.throughput

        where:
            name      = RestDslSpec.newEntityName()
            type      = HashMap
            generator = { it.field = "field-${it.id}".toString() }
    }

    def "GET - typed entity"() {
        given:
            def entityFixture = new EntityFixture<Bus>(config.numberOfEntities, type, generator)

        and:
            app([RestDslSpec.entity(name, entityFixture.data)])

        and:
            def httpFixture = new HttpFixture<Bus>("${application.address}api/$name", entityFixture,
                HttpFixture.Method.GET)

        when:
            runner.run(httpFixture)

        then:
            config.acceptableSuccessRate < runner.benchmark.successRate
            config.acceptableThroughput  < runner.benchmark.throughput

        where:
            type      = Bus
            name      = RestDslSpec.newEntityName()
            generator = { Bus bus -> bus.colour = 'red'; bus.name = bus.id }
    }

    def "POST - untyped entity"() {
        given:
            def entityFixture = new EntityFixture<HashMap>(0, type, generator)

        and:
            app([RestDslSpec.entity(name, [])])

        and:
            def httpFixture = new HttpFixture<HashMap>("${application.address}api/$name", entityFixture,
                HttpFixture.Method.POST)

        when:
            runner.run(httpFixture)

        then:
            config.acceptableSuccessRate < runner.benchmark.successRate
            config.acceptableThroughput  < runner.benchmark.throughput

        where:
            name      = RestDslSpec.newEntityName()
            type      = HashMap
            generator = { it.field = "field-${it.id}".toString() }
    }

    def "POST - typed entity"() {
        given:
            def entityFixture = new EntityFixture<Bus>(0, type, generator)

        and:
            app([RestDslSpec.entity(name, [])])

        and:
            def httpFixture = new HttpFixture<HashMap>("${application.address}api/$name", entityFixture,
                HttpFixture.Method.POST)

        when:
            runner.run(httpFixture)

        then:
            config.acceptableSuccessRate < runner.benchmark.successRate
            config.acceptableThroughput  < runner.benchmark.throughput

        where:
            name      = RestDslSpec.newEntityName()
            type      = Bus
            generator = { Bus bus -> bus.colour = 'red'; bus.name = bus.id }
    }

    final static class Config {

        int duration = 5000
        int numberOfEntities = 500

        int acceptableSuccessRate = 99
        int acceptableThroughput  = 100

    }

}


