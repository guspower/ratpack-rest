package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.handling.Context
import ratpack.handling.Handler

import static ratpack.jackson.Jackson.json as jackson

@Slf4j
class RestHandler implements Handler {

    private RestEntity entity

    RestHandler(RestEntity entity)  {
        this.entity = entity
    }

    @Override
    void handle(Context context) throws Exception {

        context.with {
            String id = pathTokens.id

            if(request.method.get) {
                if(id) {
                    get context, id
                } else {
                    getAll context
                }
            } else if(request.method.post) {
                post context
            }
        }
    }

    private void get(Context context, String id) {
        def entity = entity.store.get(id)
        if(entity) {
            context.render jackson(entity)
        } else {
            context.clientError(404)
        }
    }

    private void getAll(Context context) {
        context.render jackson(entity.store.all)
    }

    private void post(Context context) {
        String id = entity.store.create()

        context.with {
            response.headers.add 'location', "/api/${entity.name}/$id"
            response.status(201)
            response.send()
        }

    }

}
