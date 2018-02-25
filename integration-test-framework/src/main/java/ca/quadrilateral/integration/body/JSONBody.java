package ca.quadrilateral.integration.body;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.integration.MediaType;
import ca.quadrilateral.integration.util.json.JSONPrettyPrintWriter;
import ca.quadrilateral.integration.util.json.JSONWrapper;

public class JSONBody implements IBody {
    private static final Logger logger = LoggerFactory.getLogger(JSONBody.class);
    
    private final JSONWrapper json;
    private final Charset encoding;
    
    public JSONBody(final JSONAware json, final Charset encoding) {
        this.json = new JSONWrapper(json);
        this.encoding = encoding;
    }

    public JSONArray getJSONArray() {
        final JSONAware wrappedJson = json.getWrappedJson();
        
        if (JSONArray.class.isAssignableFrom(wrappedJson.getClass())) {
            return (JSONArray)wrappedJson;
        } else {
            throw new RuntimeException("JSON does not represent an array");
        }
    }
    
    public JSONObject getJSONObject() {
        final JSONAware wrappedJson = json.getWrappedJson();
        
        if (JSONObject.class.isAssignableFrom(wrappedJson.getClass())) {
            return (JSONObject)wrappedJson;
        } else {
            throw new RuntimeException("JSON does not represent an object");
        }
    }
    
    @Override
    public byte[] getBytes() {
        return json.toJSONString().getBytes(encoding);
    }
    
    @Override
    public HttpEntity getHttpEntity() {
        final StringEntity entity = new StringEntity(toString(), encoding);
        entity.setContentType(MediaType.APPLICATION_JSON.getMediaTypeString());
        return entity;
    }
    
    @Override
    public String toString() {
        try {
            final StringWriter stringWriter = new StringWriter();
            json.writeJSONString(new JSONPrettyPrintWriter(stringWriter));
            return stringWriter.toString();
        } catch (final IOException e) {
            logger.warn("Error pretty printing JSON object, defaulting to non-pretty implementation", e);
            return json.toJSONString();
        }
    }
}
