package ca.quadrilateral.integration;

public enum RequestHeader {
    ACCEPT,
    ACCEPT_CHARSET,
    ACCEPT_ENCODING,
    ACCEPT_LANGUAGE,
    ACCEPT_DATETIME,
    AUTHORIZATION,
    CACHE_CONTROL,
    CONNECTION,
    COOKIE,
    CONTENT_LENGTH,
    CONTENT_MD5,
    CONTENT_TYPE,
    DATE,
    EXPECT,
    FROM,    
    HOST,
    IF_MATCH,
    IF_MODIFIED_SINCE,
    IF_NONE_MATCH,
    IF_RANGE,
    IF_UNMODIFIED_SINCE,
    MAX_FORWARDS,
    ORIGIN,
    PRAGMA,
    PROXY_AUTHORIZATION,
    RANGE,
    REFERER,
    TE,
    USER_AGENT,
    UPGRADE,
    VIA,
    WARNING,

    X_REQUESTED_WITH,
    DNT,
    X_FORWARDED_FOR,
    X_FORWARDED_HOST,
    X_FORWARDED_PROTO,
    FRONT_END_HTTPS,
    X_HTTP_METHOD_OVERRIDE,
    X_ATT_DEVICEID,
    X_WAP_PROFILE,
    PROXY_CONNECTION;
    
    public String getHeaderKeyString() {
        return toString().replace("_", "-");
    }
}
