package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.handling.Context
import ratpack.handling.Handler

import javax.validation.ConstraintViolationException

import static ratpack.jackson.Jackson.json as toJson
import static ratpack.jackson.Jackson.fromJson as fromJson
import static org.apache.http.HttpStatus.*

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
            } else if(request.method.put) {
                id ? put(context, id) : putAll(context)
            }
        }
    }

    private void get(Context context, String id) {
        def entity = entity.store.get(id)
        if(entity) {
            context.render toJson(entity)
        } else {
            context.clientError SC_NOT_FOUND
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
                response.status SC_NO_CONTENT
                response.send()
            }
        } else {
            context.clientError SC_NOT_FOUND
        }
    }

    private void put(Context context, String id) {
        if(entity.store.get(id)) {
            if (isJsonRequest(context)) {
                def data = context.parse(fromJson(entity.store.type))
                try {
                    if (entity.store.update(id, data)) {
                        context.clientError SC_ACCEPTED
                    } else {
                        context.clientError SC_NOT_MODIFIED
                    }
                } catch(ConstraintViolationException validation) {
                    validationResponse context, validation
                }
            } else {
                context.clientError SC_NOT_MODIFIED
            }
        } else {
            context.clientError SC_NOT_FOUND
        }
    }

    private void putAll(Context context) {
        if (isJsonRequest(context)) {
            def data = context.parse(fromJson(entity.store.type))
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
                response.status SC_CREATED
                response.send()
            }
        } catch(ConstraintViolationException validation) {
            validationResponse context, validation
        }

    }

    private boolean isJsonRequest(Context context) {
        context.request.headers['content-type'] == 'application/json'
    }

    private void validationResponse(Context context, ConstraintViolationException validation) {
        context.with {
            response.status SC_BAD_REQUEST
            render toJson(ConstraintFailure.build(validation))
        }
    }

}
