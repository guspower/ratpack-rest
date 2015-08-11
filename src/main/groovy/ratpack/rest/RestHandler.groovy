package ratpack.rest

import com.fasterxml.jackson.databind.JsonMappingException
import groovy.util.logging.Slf4j
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
                id ? post(id, context) : post(context)
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
                try {
                    def data = context.parse(fromJson(entity.store.type))
                    context.blocking {
                        try {
                            entity.store.update(id, data)
                        } catch(ConstraintViolationException validation) {
                            validationResponse context, ConstraintFailure.constraintViolation(validation)
                        }
                    }.then { boolean success ->
                        if (success) {
                            context.clientError SC_ACCEPTED
                        } else {
                            context.clientError SC_NOT_MODIFIED
                        }
                    }
                } catch(JsonMappingException deserialization) {
                    validationResponse context, ConstraintFailure.jsonMapping(deserialization, entity)
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
        } else {
            validationResponse context, ConstraintFailure.noClientSuppliedId(entity.name)
        }
    }

    private void post(String id, Context context) {
        validationResponse context, ConstraintFailure.clientSuppliedId(entity.name, id)
    }

    private void post(Context context) {
        def data
        if(isJsonRequest(context)) {
            try {
                data = context.parse(fromJson(entity.store.type))
            } catch(JsonMappingException deserialization) {
                validationResponse context, ConstraintFailure.jsonMapping(deserialization, entity)
            }
        }

        if (data?.id) {
            post data.id, context
        } else {
            context.blocking {
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
            response.status SC_BAD_REQUEST
            render toJson(failures)
        }
    }

}
