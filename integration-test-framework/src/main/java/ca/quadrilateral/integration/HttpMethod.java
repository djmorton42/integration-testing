package ca.quadrilateral.integration;


public enum HttpMethod {
    GET(false),
    PUT(true),
    DELETE(false),
    POST(true),
    OPTIONS(false),
    HEAD(false),
    TRACE(false),
    PATCH(true);
    
    private final boolean supportsRequestBody;
    
    private HttpMethod(final boolean supportsRequestBody) {
        this.supportsRequestBody = supportsRequestBody;
    }
    
    public boolean supportsRequestBody() {
        return this.supportsRequestBody;
    }
}
