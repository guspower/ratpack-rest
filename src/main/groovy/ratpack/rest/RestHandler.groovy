package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.handling.Context
import ratpack.handling.Handler

import javax.validation.ConstraintViolationException

import static ratpack.jackson.Jackson.json as toJson
import static ratpack.jackson.Jackson.fromJson as fromJson
import static org.apache.http.HttpStatus.*

@Slf4j
class RestHandler implements Handler {

    private DefaultRestEntity entity

    RestHandler(DefaultRestEntity entity)  {
        this.entity = entity
    }

    @Override
    void handle(Context context) throws Exception {
        context.with {
            String id = pathTokens.id

            if(request.method.get) {
                id ? get(context, id) : getAll(context)
            } else if(request.method.post) {
                id ? post(context, id) : post(context)
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
            context.response.status SC_NOT_FOUND
            context.response.send()
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
            context.response.status SC_NOT_FOUND
            context.response.send()
        }
    }

    private void put(Context context, String id) {
        if(entity.store.get(id)) {
            if (isJsonRequest(context)) {
                context.parse(fromJson(entity.store.type)).onError { e ->
                    validationResponse context, ConstraintFailure.jsonMapping(e.cause, entity)
                }.then { data ->
                    Blocking.get {
                        try {
                            entity.store.update(id, data)
                        } catch(ConstraintViolationException validation) {
                            validationResponse context, ConstraintFailure.constraintViolation(validation)
                        }
                    }.then { boolean success ->
                        if (success) {
                            context.response.status SC_ACCEPTED
                            context.response.send()
                        } else {
                            context.response.status SC_NOT_MODIFIED
                            context.response.send()
                        }
                    }
                }
            } else {
                context.response.status SC_NOT_MODIFIED
                context.response.send()
            }
        } else {
            context.response.status SC_NOT_FOUND
            context.response.send()
        }
    }

    private void putAll(Context context) {
        if (isJsonRequest(context)) {
            context.parse(fromJson(entity.store.type)).then { data ->

            }
        } else {
            validationResponse context, ConstraintFailure.noClientSuppliedId(entity.name)
        }
    }

    private void post(Context context, String id) {
        validationResponse context, ConstraintFailure.clientSuppliedId(entity.name, id)
    }

    private void post(Context context) {
        if(isJsonRequest(context)) {
            context.parse(fromJson(entity.store.type)).onError { e ->
                validationResponse context, ConstraintFailure.jsonMapping(e.cause, entity)
            }.then { data ->
                create context, data
            }
        } else {
            create context
        }
    }

    private void create(Context context, def data = [:]) {
        if(data.id) {
            post context, data.id
        } else {
            Blocking.get {
                try {
                    entity.store.create(data)
                } catch (ConstraintViolationException validation) {
                    validationResponse context, ConstraintFailure.constraintViolation(validation)
                }
            }.then { String id ->
                context.with {
                    response.headers.add 'location', "/api/${entity.name}/$id"
                    response.status SC_CREATED
                    response.send()
                }
            }
        }
    }

    private boolean isJsonRequest(Context context) {
        context.request.headers['content-type'] == 'application/json'
    }

    private void validationResponse(Context context, List<ConstraintFailure> failures) {
        context.with {
            context.response.status SC_BAD_REQUEST
            render toJson(failures)
        }
    }

}
