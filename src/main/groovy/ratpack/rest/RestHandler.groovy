package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.handling.Context
import ratpack.handling.Handler

import javax.validation.ConstraintViolationException

import static ratpack.jackson.Jackson.json as toJson
import static ratpack.jackson.Jackson.fromJson as fromJson

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
                id ? get(context, id) : getAll(context)
            } else if(request.method.post) {
                post context
            } else if(request.method.delete) {
                id ? delete(context, id) : deleteAll(context)
            }
        }
    }

    private void get(Context context, String id) {
        def entity = entity.store.get(id)
        if(entity) {
            context.render toJson(entity)
        } else {
            context.clientError(404)
        }
    }

    private void getAll(Context context) {
        context.render toJson(entity.store.all)
    }

    private void delete(Context context, String id) {
        deleteResponse context, entity.store.delete(id)
    }

    private void deleteAll(Context context) {
        deleteResponse context, entity.store.deleteAll()
    }

    private void deleteResponse(Context context, boolean deleted) {
        if(deleted) {
            context.with {
                response.status(200)
                response.send()
            }
        } else {
            context.clientError(404)
        }
    }

    private void post(Context context) {
        def data
        if(isJsonRequest(context)) {
            data = context.parse(fromJson(entity.store.type))
        }

        try {
            String id = entity.store.create(data)

            context.with {
                response.headers.add 'location', "/api/${entity.name}/$id"
                response.status(201)
                response.send()
            }
        } catch(ConstraintViolationException validation) {
            context.with {
                response.status(400)
                render toJson(ConstraintFailure.build(validation))
            }
        }

    }

    private boolean isJsonRequest(Context context) {
        context.request.headers['content-type'] == 'application/json'
    }

}
