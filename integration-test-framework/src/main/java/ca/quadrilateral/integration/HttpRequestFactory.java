package ca.quadrilateral.integration;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;

public class HttpRequestFactory {
    public static HttpRequestBase getRequestObject(final HttpMethod method) {
        if (method == HttpMethod.DELETE) {
            return new HttpDelete();
        } else if (method == HttpMethod.GET) {
            return new HttpGet();
        } else if (method == HttpMethod.HEAD) {
            return new HttpHead();
        } else if (method == HttpMethod.OPTIONS) {
            return new HttpOptions();
        } else if (method == HttpMethod.PATCH) {
            return new HttpPatch();
        } else if (method == HttpMethod.POST) {
            return new HttpPost();
        } else if (method == HttpMethod.PUT) {
            return new HttpPut();
        } else if (method == HttpMethod.TRACE) {
            return new HttpTrace();
        } else {
            throw new AssertionError("Invalid HTTP method type: " + method);
        }
        
    }
}
