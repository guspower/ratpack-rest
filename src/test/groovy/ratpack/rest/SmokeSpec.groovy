package ratpack.rest

import ratpack.rest.fixture.RestApplicationUnderTest
import ratpack.test.http.TestHttpClient
import spock.lang.AutoCleanup
import spock.lang.Specification

class SmokeSpec extends Specification {

    @AutoCleanup
    def aut = new RestApplicationUnderTest()

    @Delegate
    TestHttpClient client = testHttpClient(aut)

    def "application is up and running"() {
        when:
            get()

        then:
            response.statusCode == 200
            response.body.text == "Hello World!"
    }

}
