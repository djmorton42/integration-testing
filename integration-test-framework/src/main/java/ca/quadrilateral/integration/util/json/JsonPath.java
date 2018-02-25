package ca.quadrilateral.integration.util.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class JsonPath {
    private final JSONWrapper wrapper;
    
    private Pattern arrayIndexPattern = Pattern.compile("\\[([0-9])\\]");
    
    public JsonPath(final String json) {
        this.wrapper = new JSONWrapper(json);
    }
    
    public <T> T get(final String path, final Class<?> type) {
        final List<String> pathElements = getPathElements(path);
        
        Object result = wrapper.getWrappedJson();
        for (final String pathElement : pathElements) {
            result = getItem(result, pathElement);
        }
        
        return (T)result;
    }
    
    public Object getItem(final Object jsonObject, final String path) {
        final Matcher matcher = arrayIndexPattern.matcher(path);
        
        if (matcher.matches()) {
            if (JSONArray.class.isAssignableFrom(jsonObject.getClass())) {
                int arrayIndex = Integer.parseInt(matcher.group(1));
                return ((JSONArray)jsonObject).get(arrayIndex);
            } else {
                throw new RuntimeException("Path was an array index but element was not an array");
            }   
        } else {
            if (JSONObject.class.isAssignableFrom(jsonObject.getClass())) {
                return ((JSONObject)jsonObject).get(path);
            } else {
                throw new RuntimeException("Path was an object property but element was not an object");
            }
        }
    }
    
    public List<String> getPathElements(final String path) {
        final List<String> pathElements = new ArrayList<>();
        
        String currentElement = "";
        for (int i = 0; i < path.length(); i++) {
            final char currentCharacter = path.charAt(i);
            
            if (currentCharacter == '.') {
                if (StringUtils.isNotBlank(currentElement)) {
                    pathElements.add(currentElement);
                    currentElement = "";
                }
            } else if (currentCharacter == '[') {
                if (StringUtils.isNotBlank(currentElement)) {
                    pathElements.add(currentElement);
                    currentElement = "";
                }
                currentElement += currentCharacter;
            } else if (currentCharacter == ']') {
                currentElement += currentCharacter;
                pathElements.add(currentElement);
                currentElement = "";
            } else {
                currentElement += currentCharacter;
            }
        }
        
        if (StringUtils.isNotBlank(currentElement)) {
            pathElements.add(currentElement);
        }
        
        return pathElements;
    }
    
    public static void main(String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new FileReader("/tmp/json.txt"));
        
        String inLine = null;
        while ((inLine = reader.readLine()) != null) {
            builder.append(inLine);
        }   
        reader.close();
        
        final JsonPath jsonPath =  new JsonPath(builder.toString());
        
        System.out.println(
                jsonPath.get("[0].sessions[0].sittings[0].client.contact.phone", String.class).toString()
            );
    }        
}
