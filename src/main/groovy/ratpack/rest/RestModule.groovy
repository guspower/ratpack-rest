package ratpack.rest

import com.google.inject.Provides
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import ratpack.guice.ConfigurableModule
import ratpack.rest.store.EntityStore
import ratpack.rest.store.InMemoryEntityStore

@Slf4j
class RestModule extends ConfigurableModule<RestModule.Config> {

    @ToString(includePackage = false, includeNames = true)
    static class Config {

        List<RestEntity> entities = []

        @Provides
        List<RestEntity> restEntities() {
            entities
        }

    }

    @ToString(includePackage = false, includeNames = true)
    static class RestEntity {
        EntityStore store = new InMemoryEntityStore<?>()
        String name
    }

    @Override
    protected void configure() {
        bind RestHandlers
    }

}
