package ratpack.rest

import com.google.inject.Inject
import ratpack.groovy.handling.GroovyChain
import ratpack.handling.Context

import static org.apache.http.HttpStatus.*

class RestModuleHandlers {

    private RestModule.Config config

    @Inject
    RestModuleHandlers(RestModule.Config config) {
        this.config = config
    }

    void all(GroovyChain chain) {
        chain.path "${config.relationPath}/:relationName/:id?", RelationHandler
        config.entities.each { DefaultRestEntity entity ->
            chain.path "${config.resourcePath}/${entity.name}/:id?", new ResourceHandler(entity, config)
        }

        def notFound = { Context context ->
            context.response.status(SC_NOT_FOUND)
            context.response.send()
        }

        chain.path "${config.resourcePath}/:entity?/:id?", notFound
        chain.path "${config.relationPath}/:entity?/:id?", notFound
    }

}
