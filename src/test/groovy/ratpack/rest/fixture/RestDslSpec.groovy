package ratpack.rest.fixture

import ratpack.jackson.guice.JacksonModule
import ratpack.rest.RestEntity
import ratpack.rest.RestHandlers
import ratpack.rest.RestModule
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore
import ratpack.test.internal.RatpackGroovyDslSpec

class RestDslSpec extends RatpackGroovyDslSpec {

    void app() {
        app([])
    }

    void app(List<RestEntity> entities) {
        bindings {
            module JacksonModule
            module RestModule, { RestModule.Config config ->
                config.entities.addAll entities
            }
        }
        handlers { RestHandlers rest ->
            rest.register delegate
        }
    }

    EntityStore store(Class type, List data) {
        data ? new InMemoryEntityStore(type, data) : new InMemoryEntityStore(type)
    }

    RestEntity entity(String name, List data) {
        new RestEntity(name, store(HashMap.class, data))
    }

    RestEntity entity(Class type, List data) {
        new RestEntity(type, store(type, data))
    }

    String newEntityName() {
        "entity-${UUID.randomUUID()}"
    }

    String newId() {
        UUID.randomUUID().toString()
    }

}
