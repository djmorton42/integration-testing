package ca.quadrilateral.integration;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.integration.body.BinaryBody;
import ca.quadrilateral.integration.body.IBody;
import ca.quadrilateral.integration.body.JSONBody;
import ca.quadrilateral.integration.body.NoBody;
import ca.quadrilateral.integration.body.TextBody;

public class Request {
    private static final Logger logger = LoggerFactory.getLogger(Request.class);
    
    public final HttpMethod method;
    public final URI uri;
    public final IBody requestBody;
    public final Map<String, String> headerMap;

    private Request(final HttpMethod method, final URI uri) {
        this(method, uri, new NoBody(), Collections.emptyMap());
    }
    
    private Request(final HttpMethod method, final URI uri, final Map<String, String> headerMap) {
        this(method, uri, new NoBody(), headerMap);
    }   

    private Request(final HttpMethod method, final URI uri, final IBody requestBody) {
        this(method, uri, requestBody, Collections.emptyMap());
    }
    
    private Request(final HttpMethod method, final URI uri, final IBody requestBody, final Map<String, String> headerMap) {
        this.method = method;
        this.uri = uri;
        this.headerMap = headerMap;
        this.requestBody = requestBody;
    }
    
    public HttpEntity getHttpEntity() {
        return requestBody.getHttpEntity();
    }
    
    public boolean hasBodyEntity() {
        return !NoBody.class.isAssignableFrom(requestBody.getClass());
    }
    
    public static class RequestBuilder {
        private static Charset defaultCharset = Charset.defaultCharset();
        
        private HttpMethod method;
        private IUri uri;
        private IBody requestBody = new NoBody();
        
        private Map<String, String> headerMap = new HashMap<>();
        
        public static RequestBuilder getInstance() {
            return new RequestBuilder();
        }
        
        public static void setDefaultCharset(final Charset charset) {
            defaultCharset = charset;
        }
        
        public static Charset getDefaultCharset() {
            return defaultCharset;
        }
        
        private RequestBuilder() {}
        
        public RequestBuilder setHttpMethod(final HttpMethod method) {
            this.method = method;
            return this;
        }
        
        public RequestBuilder setUrl(final String uri) {
            this.uri = new StringUri(uri);
            return this;
        }
        
        public RequestBuilder setUri(final URI uri) {
            this.uri = new ObjectUri(uri);
            return this;
        }
        
        public RequestBuilder setUri(final URIBuilder uriBuilder) throws RequestBuilderException {
            this.uri = new UriBuilderUri(uriBuilder);
            return this;
        }
            
        public RequestBuilder addHeader(final RequestHeader headerKey, final String headerValue) {
            this.headerMap.put(headerKey.getHeaderKeyString(), headerValue);
            return this;
        }
        
        public RequestBuilder addHeader(final String headerKey, final String headerValue) {
            this.headerMap.put(headerKey, headerValue);
            return this;
        }
        
        public RequestBuilder setRequestBody(final byte[] bytes) {
            this.requestBody = new BinaryBody(bytes);
            return this;
        }
        
        public RequestBuilder setRequestBody(final String bodyText, final Charset encoding) {
            this.requestBody = new TextBody(bodyText, encoding);
            return this;
        }
        
        public RequestBuilder setRequestBody(final String bodyText) {
            this.requestBody = new TextBody(bodyText, defaultCharset);
            return this;
        }
        
        public RequestBuilder setRequestBody(final JSONAware json, final Charset encoding) {
            this.requestBody = new JSONBody(json, encoding);
            return this;
        }
        
        public RequestBuilder setRequestBody(final JSONAware json) {
            this.requestBody = new JSONBody(json, defaultCharset);
            return this;
        }
        
        public Request build() throws RequestBuilderException {
            testValidity();
            
            try {
                return new Request(method, uri.getUri(), requestBody, headerMap);
            } catch (final URISyntaxException e) {
                throw new RequestBuilderException(e);
            }
        }   
        
        private void testValidity() throws RequestBuilderException {
            if (method == null) {
                throw new RequestBuilderException("An HTTP method must be specified");
            }
            
            if (uri == null) {
                throw new RequestBuilderException("A URI must be specified");
            }
            
            if (!isBodyValidForMethod()) {
                throw new RequestBuilderException("The HTTP method " + method + " may not specify a request body entity");
            }
            
        }
        
        private boolean isBodyValidForMethod() {
            return NoBody.class.isAssignableFrom(requestBody.getClass())
                ? true
                : method.supportsRequestBody();
        }   
    }

    private static interface IUri {
        URI getUri() throws URISyntaxException;
    }
    
    private static class StringUri implements IUri {
        private final String uriString;
        
        public StringUri(final String uriString) {
            this.uriString = uriString;
        }
        
        public URI getUri() throws URISyntaxException {
            return new URI(uriString);
        }
    }
    
    private static class ObjectUri implements IUri {
        private final URI uri;
        
        public ObjectUri(final URI uri) {
            this.uri = uri;
        }
        
        public URI getUri() throws URISyntaxException {
            return this.uri;
        }
    }
    
    private static class UriBuilderUri implements IUri {
        private final URIBuilder uriBuilder;
        
        public UriBuilderUri(final URIBuilder uriBuilder) {
            this.uriBuilder = uriBuilder;
        }
        
        public URI getUri() throws URISyntaxException {
            return uriBuilder.build();
        }
    }
}
