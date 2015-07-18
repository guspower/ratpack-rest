package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.handling.Context
import ratpack.handling.Handler

import static ratpack.jackson.Jackson.json as jackson

@Slf4j
class RestHandler implements Handler {

    private RestModule.RestEntity entity

    RestHandler(RestModule.RestEntity entity)  {
        this.entity = entity
    }

    @Override
    void handle(Context context) throws Exception {
        String id = context.pathTokens.id

        if(id) {
            def entity = entity.store.get(id)
            if(entity) {
                context.render jackson(entity)
            } else {
                context.clientError(404)
            }
        } else {
            context.render jackson(entity.store.all)
        }
    }

}
