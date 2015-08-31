package ratpack.rest.fixture

import com.google.inject.Injector
import com.google.inject.Module
import groovy.util.logging.Slf4j
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.groovy.Groovy
import ratpack.groovy.handling.GroovyChain
import ratpack.groovy.internal.ClosureUtil
import ratpack.guice.BindingsSpec
import ratpack.guice.Guice
import ratpack.handling.Context
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.DefaultRestEntity
import ratpack.rest.RestModuleHandlers
import ratpack.rest.RestModule
import ratpack.rest.store.InMemoryRelationStore
import ratpack.rest.store.RelationStore
import ratpack.server.RatpackServer
import ratpack.server.ServerConfig
import ratpack.server.ServerConfigBuilder
import ratpack.test.embed.EmbeddedApp
import spock.lang.Specification

@Slf4j
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
            bind RelationStore, InMemoryRelationStore
            bind ServerErrorHandler, ErrorHandler
            bind ClientErrorHandler, ErrorHandler
        }
        handlers { RestModuleHandlers rest ->
            rest.all delegate
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

    protected ServerConfigBuilder serverConfigBuilder() {
        def config = ServerConfig.builder()
            .port(0)

        config.with(_serverConfig)
        config
    }

    void handlers(@DelegatesTo(value = GroovyChain, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _handlers = configurer
    }

    void bindings(@DelegatesTo(value = BindingsSpec, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _bindings = configurer
    }

    void serverConfig(@DelegatesTo(value = ServerConfigBuilder, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer) {
        _serverConfig = configurer
    }

    def cleanup() {
        application?.server?.stop()
    }

    String currentTestName() {
        "${this.class.simpleName}-${specificationContext.currentFeature.name}".replaceAll('\\W', '-')
    }

}

@Slf4j
class ErrorHandler implements ClientErrorHandler, ServerErrorHandler {

    @Override
    void error(Context context, int statusCode) throws Exception {
        log.warn "ERROR - $statusCode"
    }

    @Override
    void error(Context context, Throwable throwable) throws Exception {
        log.warn context.request.uri, throwable
    }
}
