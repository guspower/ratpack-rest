package ratpack.rest.fixture

import com.google.inject.Injector
import com.google.inject.Module
import ratpack.groovy.Groovy
import ratpack.groovy.handling.GroovyChain
import ratpack.groovy.internal.ClosureUtil
import ratpack.guice.BindingsSpec
import ratpack.guice.Guice
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.DefaultRestEntity
import ratpack.rest.RestHandlers
import ratpack.rest.RestModule
import ratpack.server.RatpackServer
import ratpack.server.ServerConfig
import ratpack.test.embed.EmbeddedApp
import spock.lang.Specification

class OnDemandApplicationSpec extends Specification {

    protected EmbeddedApp application

    protected final List<Module> modules = []
    protected Closure<?> _handlers = ClosureUtil.noop()
    protected Closure<?> _bindings = ClosureUtil.noop()
    protected Closure<?> _serverConfig = ClosureUtil.noop()
    protected Injector parentInjector

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

    def cleanup() {
        application?.server?.stop()
    }

    String currentTestName() {
        "${this.class.simpleName}-${specificationContext.currentFeature.name}".replaceAll('\\W', '-')
    }

}
