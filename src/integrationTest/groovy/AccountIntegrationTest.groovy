import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import spock.lang.Specification

class AccountIntegrationTest extends Specification {

    OkHttpClient client
    String url = "http://localhost:4567"

    def setup() {
        this.client = new OkHttpClient()
    }

    def "return 404 if account does not exist" () {
        given:
        Request request = new Request.Builder()
                .url(url + "/account/1")
                .build()

        when:
        Response response = this.client.newCall(request).execute()

        then:
        response.code() == 404
    }
}