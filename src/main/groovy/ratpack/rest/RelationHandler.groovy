package ratpack.rest

import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.rest.store.Relation
import ratpack.rest.store.RelationStore

import javax.inject.Inject

import static org.apache.http.HttpStatus.SC_ACCEPTED
import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_CREATED
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED
import static org.apache.http.HttpStatus.SC_NO_CONTENT
import static ratpack.jackson.Jackson.fromJson
import static ratpack.jackson.Jackson.json as toJson

class RelationHandler implements Handler {

    private RelationStore relationStore
    private RestModule.Config config

    @Inject
    RelationHandler(RelationStore relationStore, RestModule.Config config) {
        this.relationStore = relationStore
        this.config = config
    }

    @Override
    void handle(Context context) throws Exception {
        context.with {
            String name = pathTokens.relationName
            String id = pathTokens.id

            context.byMethod { m ->
                m.delete {
//                    id ? delete(context, name, id) : deleteAll(context, name)
                }
                m.get    { id ? find(context, name, id) : findAll(context, name) }
                m.post   { id ? failCreate(context, name, id) : create(context, name) }
                m.put    {
//                    id ? update(context, name, id) : failUpdate(context)
                }
            }
        }
    }

    private void find(Context context, String name, String id) {
        Blocking.get {
            relationStore.get(id)
        }.then { Relation relation ->
            context.render toJson(relation)
        }
    }

    private void findAll(Context context) {

    }

    private void create(Context context, String name) {
        parse(context).then { CreateRelation data ->
            data.name = name
            create context, data
        }
    }

    private void create(Context context, CreateRelation data) {
        Blocking.get {
            relationStore.create data
        }.then { String id ->
            context.with {
                response.headers.add 'location', getRelationPath(data.name, id)
                created context
            }
        }
    }

    void failCreate(Context context, String name, String id) {
        fail context, ConstraintFailure.clientSuppliedId(name, id)
    }

    private Promise parse(Context context) {
        if(isJsonRequest(context)) {
            context.parse(fromJson(CreateRelation)).onError { e ->
                fail context, ConstraintFailure.jsonMapping(e.cause, entity)
            }
        } else {
            Promise.value([:])
        }
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

    private String getRelationPath(String name, String id) {
        "/${config.relationPath}/${name}/$id"
    }

}
