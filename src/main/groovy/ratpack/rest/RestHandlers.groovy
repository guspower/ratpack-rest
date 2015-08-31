package ratpack.rest

import com.google.inject.Inject
import ratpack.groovy.handling.GroovyChain
import ratpack.handling.Context

import static org.apache.http.HttpStatus.*

class RestHandlers {

    private RestModule.Config config

    @Inject
    RestHandlers(RestModule.Config config) {
        this.config = config
    }

    void register(GroovyChain chain) {
        chain.path "api/relation/:id?", new RelationHandler()
        config.entities.each { DefaultRestEntity entity ->
            chain.path "api/${entity.name}/:id?", new RestHandler(entity)
        }
        chain.path "api/:unknown/:id?", { Context context ->
            context.response.status(SC_NOT_FOUND)
            context.response.send()
        }
    }

}
