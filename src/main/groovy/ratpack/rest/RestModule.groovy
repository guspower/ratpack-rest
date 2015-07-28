package ratpack.rest

import com.google.inject.Provides
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import ratpack.guice.ConfigurableModule

@Slf4j
class RestModule extends ConfigurableModule<RestModule.Config> {

    @ToString(includePackage = false, includeNames = true)
    static class Config {

        List<DefaultRestEntity> entities = []

        @Provides
        List<DefaultRestEntity> restEntities() {
            entities
        }

    }

    @Override
    protected void configure() {
        bind RestHandlers
    }

}
