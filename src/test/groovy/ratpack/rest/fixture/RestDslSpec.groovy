package ratpack.rest.fixture

import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.jackson.guice.JacksonModule
import ratpack.rest.DefaultRestEntity
import ratpack.rest.Resource
import ratpack.rest.RestModuleHandlers
import ratpack.rest.RestModule
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore
import ratpack.rest.store.InMemoryRelationStore
import ratpack.rest.store.RelationStore
import ratpack.test.internal.RatpackGroovyDslSpec

class RestDslSpec extends RatpackGroovyDslSpec {

    void app() {
        app([])
    }

    void app(List<DefaultRestEntity> entities) {
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
    }

    static EntityStore store(Class type, List data) {
        data ? new InMemoryEntityStore(type, data) : new InMemoryEntityStore(type)
    }

    static Resource entity(String name, List data) {
        new DefaultRestEntity(name, store(HashMap.class, data))
    }

    static Resource entity(Class type, List data) {
        new DefaultRestEntity(type, store(type, data))
    }

    static Resource entity(String name, Class type, List data) {
        new DefaultRestEntity(name, type, store(type, data))
    }

    static String newEntityName() {
        "entity-${UUID.randomUUID()}"
    }

    static String newId() {
        UUID.randomUUID().toString()
    }

    protected String path(String entityName) {
        "/${new RestModule.Config().resourcePath}/$entityName"
    }

}
