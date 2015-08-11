package ratpack.rest.fixture

import jodd.http.HttpRequest
import jodd.http.HttpResponse

import static org.apache.http.HttpStatus.*

class HttpFixture<T> implements Cloneable {

    private final String baseUrl
    private final EntityFixture<T> entityFixture
    private final Method method

    HttpFixture(String baseUrl, EntityFixture<T> entityFixture, Method method) {
        this.baseUrl = baseUrl
        this.entityFixture = entityFixture
        this.method = method
    }

    boolean next() {
        switch(method) {
            case Method.GET:  getNext();  break;
            case Method.POST: postNext(); break;
        }
    }

    boolean getNext() {
        T instance = entityFixture.pick()
        HttpResponse response = HttpRequest.get("$baseUrl/${instance.id}").send()
        (SC_OK == response.statusCode()) && (instance == entityFixture.hydrate(response.body()))
    }

    boolean postNext() {
        HttpRequest request = HttpRequest.post("$baseUrl")
        request.contentType 'application/json'
        request.body entityFixture.dehydrate(entityFixture.nextWithoutIdentity())
        HttpResponse response = request.send()
        (SC_CREATED == response.statusCode()) && response.header('location')
    }

    enum Method {
        GET,
        POST
    }

}
