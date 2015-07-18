package ratpack.rest

import com.google.inject.Inject
import ratpack.groovy.handling.GroovyChain

class RestHandlers {

    private RestModule.Config config

    @Inject
    RestHandlers(RestModule.Config config) {
        this.config = config
    }

    void register(GroovyChain chain) {
        config.entities.each { RestModule.RestEntity entity ->
            chain.path "api/${entity.name}/:id?", new RestHandler(entity)
        }
    }

}
