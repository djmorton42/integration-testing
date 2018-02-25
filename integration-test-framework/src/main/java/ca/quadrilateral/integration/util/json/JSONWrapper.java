package ca.quadrilateral.integration.util.json;

import java.io.IOException;
import java.io.Writer;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSONWrapper implements JSONAware {
    private final JSONAware json;
    
    public JSONWrapper(final JSONAware json) {
        this.json = json;
    }
    
    public JSONWrapper(final String json) {
        this.json = (JSONAware)JSONValue.parse(json);
    }
    
    public void writeJSONString(final Writer out) throws IOException {
        if (JSONObject.class.isAssignableFrom(json.getClass())) {
            ((JSONObject)json).writeJSONString(out);
        } else if (JSONArray.class.isAssignableFrom(json.getClass())) {
            ((JSONArray)json).writeJSONString(out);
        } else {
            throw new UnsupportedOperationException("writeJSONString is not supported for JSONAware type " + json.getClass());
        }
    }
    
    public boolean isObject() {
        return JSONObject.class.isAssignableFrom(json.getClass());
    }
    
    public boolean isArray() {
        return JSONArray.class.isAssignableFrom(json.getClass());
    }
    
    public JSONAware getWrappedJson() {
        return this.json;
    }

    @Override
    public String toJSONString() {
        return json.toJSONString();
    }
}