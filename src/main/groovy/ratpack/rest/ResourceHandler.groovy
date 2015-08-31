package ratpack.rest

import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.handling.Handler

import static ratpack.jackson.Jackson.json as toJson
import static ratpack.jackson.Jackson.fromJson as fromJson
import static org.apache.http.HttpStatus.*

@Slf4j
class ResourceHandler implements Handler {

    private DefaultRestEntity entity
    private RestModule.Config config

    ResourceHandler(DefaultRestEntity entity, RestModule.Config config)  {
        this.entity = entity
        this.config = config
    }

    @Override
    void handle(Context context) throws Exception {
        context.with {
            String id = pathTokens.id

            context.byMethod { m ->
                m.delete { id ? delete(context, id) : deleteAll(context) }
                m.get    { id ? find(context, id) : findAll(context) }
                m.post   { id ? failCreate(context, id) : create(context) }
                m.put    { id ? update(context, id) : failUpdate(context) }
            }
        }
    }

    private void find(Context context, String id) {
        Blocking.get {
            entity.store.get(id)
        }.then { entity ->
            entity ? context.render(toJson(entity)): context.next()
        }
    }

    private void findAll(Context context) {
        Blocking.get {
            entity.store.all
        }.then { entities ->
            context.render toJson(entities)
        }
    }

    private void delete(Context context, String id) {
        Blocking.get {
            entity.store.delete(id)
        }.then { boolean deleted ->
            deleteResponse context, deleted
        }

    }

    private void deleteAll(Context context) {
        Blocking.get {
            entity.store.deleteAll()
        }.then { boolean deleted ->
            deleteResponse context, entity.store.deleteAll()
        }
    }

    private void deleteResponse(Context context, boolean deleted) {
        deleted ? noContent(context): context.next()
    }

    private void update(Context context, String id) {
        Blocking.get {
            entity.store.exists(id)
        }.then { boolean exists ->
            if (exists) {
                parse(context).then { data ->
                    if (data) {
                        update context, id, data
                    } else {
                        notModified context
                    }
                }
            } else {
                context.next()
            }
        }
    }

    private void update(Context context, String id, def data) {
        Blocking.get {
            entity.store.update(id, data)
        }.onError { e ->
            fail context, ConstraintFailure.constraintViolation(e)
        }.then { boolean success ->
            success ? accepted(context) : notModified(context)
        }
    }

    private void failUpdate(Context context) {
        fail context, ConstraintFailure.noClientSuppliedId(entity.name)
    }

    private void create(Context context) {
        parse(context).then { def data ->
            create context, data
        }
    }

    private void create(Context context, def data) {
        if(!data.id) {
            Blocking.get {
                entity.store.create(data)
            }.onError { e ->
                fail context, ConstraintFailure.constraintViolation(e)
            }.then { String id ->
                context.with {
                    response.headers.add 'location', getResourcePath(id)
                    created context
                }
            }
        } else {
            failCreate context, data.id
        }
    }

    private Promise parse(Context context) {
        if(isJsonRequest(context)) {
            context.parse(fromJson(entity.store.type)).onError { e ->
                fail context, ConstraintFailure.jsonMapping(e.cause, entity)
            }
        } else {
            Promise.value([:])
        }
    }

    private String getResourcePath(String id) {
        "/${config.resourcePath}/${entity.name}/$id"
    }

    private void accepted(Context context)    { send context, SC_ACCEPTED }
    private void created(Context context)     { send context, SC_CREATED }
    private void noContent(Context context)   { send context, SC_NO_CONTENT }
    private void notModified(Context context) { send context, SC_NOT_MODIFIED }

    private void send(Context context, int statusCode) {
        context.with {
            response.status statusCode
            response.send()
        }
    }

    private void failCreate(Context context, def id) {
        fail context, ConstraintFailure.clientSuppliedId(entity.name, id)
    }

    private void fail(Context context, List<ConstraintFailure> failures) {
        context.with {
            context.response.status SC_BAD_REQUEST
            render toJson(failures)
        }
    }

    private boolean isJsonRequest(Context context) {
        context.request.headers['content-type'] == 'application/json'
    }

}
