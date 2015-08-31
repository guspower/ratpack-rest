package ratpack.rest.fixture

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import ratpack.http.client.RequestSpec

import static org.apache.http.HttpStatus.*

trait JsonHelper {

    private ObjectMapper _jackson = new ObjectMapper()

    ObjectMapper getJackson() { _jackson }

    JsonNode getJson(int code) {
        assert response.statusCode == code

        jackson.readTree response.body.text
    }

    JsonNode getJson() {
        getJson SC_OK
    }

    String getIdFromResponse() {
        response.headers['location'].tokenize('/')[-1]
    }

    void json() {
        requestSpec { RequestSpec spec ->
            spec.body { RequestSpec.Body body ->
                body.type('application/json')
            }
        }
    }

    void jsonPayload(Map data) {
        requestSpec { RequestSpec spec ->
            spec.body { RequestSpec.Body body ->
                body.type('application/json')
                body.text(JsonOutput.toJson(data))
            }
        }
    }

}
