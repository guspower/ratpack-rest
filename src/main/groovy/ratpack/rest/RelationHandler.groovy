package ratpack.rest

import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.rest.store.RelationStore

import static org.apache.http.HttpStatus.SC_CREATED
import static ratpack.jackson.Jackson.json as toJson

class RelationHandler implements Handler {

    private RelationStore relationStore

    RelationHandler(RelationStore relationStore) {
        this.relationStore = relationStore
    }

    @Override
    void handle(Context context) throws Exception {
        context.with {
            String id = pathTokens.id

            if(request.method.get) {
                id ? get(context, id) : getAll(context)
            } else if(request.method.post) {
                id ? post(context, id) : post(context)
//            } else if(request.method.delete) {
//                id ? delete(context, id) : deleteAll(context)
//            } else if(request.method.put) {
//                id ? put(context, id) : putAll(context)
            }
        }
    }

    void get(Context context, String id) {
        context.render toJson([id: id, from: 'from', to: 'to'])
    }
    void getAll(Context context) {}

    void post(Context context, String id) {}
    void post(Context context) {
        context.with {
            response.headers.add 'location', "/api/relation/mockId"
            response.status SC_CREATED
            response.send()
        }
    }

}
