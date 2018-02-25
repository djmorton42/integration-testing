package ca.quadrilateral.integration;

public enum MediaType {
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain");
    
    final String mediaTypeString;
    
    private MediaType(final String mediaTypeString) {
        this.mediaTypeString = mediaTypeString;
    }
    
    public String getMediaTypeString() {
        return mediaTypeString;
    }
    
    @Override
    public String toString() {
        return mediaTypeString;
    }
}
