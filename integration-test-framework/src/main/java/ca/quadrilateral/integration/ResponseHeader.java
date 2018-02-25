package ca.quadrilateral.integration;

public enum ResponseHeader {
    ACCESS_CONTROL_ALLOW_ORIGIN,
    ACCEPT_PATCH,
    ACCEPT_RANGES,
    AGE,
    ALLOW,
    CACHE_CONTROL,
    CONNECTION,
    CONTENT_ENCODING,
    CONTENT_LANGUAGE,
    CONTENT_LENGTH,
    CONTENT_LOCATION,
    CONTENT_MD5,
    CONTENT_DISPOSITION,
    CONTENT_RANGE,
    CONTENT_TYPE,
    DATA,
    ETAG,
    EXPIRES,
    LAST_MODIFIED,
    LINK,
    LOCATION,
    P3P,
    PRAGMA,
    PROXY_AUTHENTICATE,
    REFRESH,
    RETRY_AFTER,
    SERVER,
    SET_COOKIE,
    STATUS,
    STRICT_TRANSPORT_SECURITY,
    TRAILER,
    TRANSFER_ENCODING,
    UPGRADE,
    VARY,
    VIA,
    WARNING,
    WWW_AUTHENTICATE,
    X_FRAME_OPTIONS,
    PUBLIC_KEY_PINS,
    X_XSS_PROTECTION,
    CONTENT_SECURITY_POLICY,
    X_CONTENT_SECURITY_POLICY,
    X_WEBKIT_CSP,
    X_CONTENT_TYPE_OPTIONS,
    X_POWERED_BY,
    X_UA_COMPATIBLE,
    X_CONTENT_DURATION;
    
    public String getHeaderKeyString() {
        return toString().replace("_", "-");
    }
}
