package ratpack.rest

import com.google.inject.Injector
import com.google.inject.Module
import ratpack.groovy.Groovy
import ratpack.groovy.handling.GroovyChain
import ratpack.groovy.internal.ClosureUtil
import ratpack.guice.BindingsSpec
import ratpack.guice.Guice
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.fixture.Bus
import ratpack.rest.fixture.RequestGenerator
import ratpack.rest.fixture.RestDslSpec
import ratpack.server.RatpackServer
import ratpack.server.ServerConfig
import ratpack.test.embed.EmbeddedApp
import spock.lang.Specification

class MicroBenchmarkSpec extends Specification {

    protected final List<Module> modules = []
    protected Closure<?> _handlers = ClosureUtil.noop()
    protected Closure<?> _bindings = ClosureUtil.noop()
    protected Closure<?> _serverConfig = ClosureUtil.noop()
    protected Injector parentInjector

    EmbeddedApp createApplication() {
        EmbeddedApp.fromServer {
            RatpackServer.of {
                it.serverConfig(serverConfigBuilder())

                def bindingsAction = { s ->
                    s.with(_bindings)
                    modules.each { s.module(it) }
                }

                it.registry(parentInjector ? Guice.registry(parentInjector, bindingsAction) : Guice.registry(bindingsAction))
                it.handler { Groovy.chain(it, _handlers) }
            }
        }
    }

    protected ServerConfig.Builder serverConfigBuilder() {
        def serverConfig = ServerConfig.noBaseDir()
        serverConfig.port(0)
        serverConfig.with(_serverConfig)
        serverConfig
    }

    void handlers(@DelegatesTo(value = GroovyChain, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _handlers = configurer
    }

    void bindings(@DelegatesTo(value = BindingsSpec, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _bindings = configurer
    }

    void serverConfig(@DelegatesTo(value = ServerConfig.Builder, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _serverConfig = configurer
    }

    int testDuration = 5000
    EmbeddedApp application
    RequestGenerator generator

    def setup() {
        generator = new RequestGenerator()
    }

    def cleanup() {
        application.close()
        println generator.report()
    }

    EmbeddedApp app(List<DefaultRestEntity> entities) {
        bindings {
            module JacksonModule
            module RestModule, { RestModule.Config config ->
                config.entities.addAll entities
            }
        }
        handlers { RestHandlers rest ->
            rest.register delegate
        }
        Thread.start {
            application = createApplication()
        }
        while(!application) {
            Thread.yield()
        }
    }

    def "GET - untyped entity"() {
        given:
            app([RestDslSpec.entity(name, data)])

        when:
            Thread thread = generator.run("${application.address}api/$name/$id", testDuration)

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
            Thread thread = generator.run("${application.address}api/$name/${data[0].id}", testDuration)

        then:
            thread.join()

        where:
            name = RestDslSpec.newEntityName()
            data = [new Bus(id: RestDslSpec.newId(), colour: 'green', name: 'Fred')]
    }

}
