package ratpack.rest.fixture

import jodd.http.HttpRequest
import jodd.http.HttpResponse

class HttpFixture<T> implements Cloneable {

    private final String baseUrl
    private final EntityFixture<T> entityFixture

    HttpFixture(String baseUrl, EntityFixture<T> entityFixture) {
        this.baseUrl = baseUrl
        this.entityFixture = entityFixture
    }

    boolean getNext() {
        T instance = entityFixture.pick()
        HttpResponse response = HttpRequest.get("$baseUrl/${instance.id}").send()
        (200 == response.statusCode()) && (instance == entityFixture.hydrate(response.body()))
    }

}
