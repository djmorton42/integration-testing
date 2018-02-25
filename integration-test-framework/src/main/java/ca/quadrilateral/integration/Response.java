package ca.quadrilateral.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.integration.body.BinaryBody;
import ca.quadrilateral.integration.body.IBody;
import ca.quadrilateral.integration.body.JSONBody;
import ca.quadrilateral.integration.body.NoBody;
import ca.quadrilateral.integration.body.TextBody;

public class Response {
    private static final Logger logger = LoggerFactory.getLogger(Response.class);
    
    private final HttpUriRequest request;
    private final int statusCode;
    private final String statusPhrase;
    private final Map<String, Header> headerMap = new HashMap<>();
    private final IBody responseBody;
    
    public Response(final HttpUriRequest request, final HttpResponse httpResponse) {
        this.request = request;
        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.statusPhrase = httpResponse.getStatusLine().getReasonPhrase();
        
        final Header[] allHeaders = httpResponse.getAllHeaders();
        for (final Header header : allHeaders) {
            headerMap.put(header.getName(), header);
        }
        
        this.responseBody = buildResponseBody(httpResponse);
    }
    
    private IBody buildResponseBody(final HttpResponse httpResponse) {
        final HttpEntity entity = httpResponse.getEntity();

        InputStream entityInputStream = null;
        try {

            if (entity == null || httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                return new NoBody();
            }
    
            entityInputStream = entity.getContent();  
            final long contentLength = entity.getContentLength(); 
            
            final ByteArrayOutputStream outputStream;
            if (contentLength > 0 && contentLength <= Integer.MAX_VALUE) {
                outputStream = new ByteArrayOutputStream((int)contentLength);
            } else if (contentLength == 0) {
                return new NoBody();
            } else if (contentLength == -1) {
                outputStream = new ByteArrayOutputStream();
            } else {
                throw new UnsupportedOperationException("Responses of greater than " + Integer.MAX_VALUE + " bytes are presently not supported.");
            }
            
            IOUtils.copy(entity.getContent(), outputStream);
                    
            final HeaderElement[] elements = entity.getContentType().getElements();
            
            if (elements.length != 1) {
                return new BinaryBody(outputStream.toByteArray());
            } else if (elements[0].getName().equalsIgnoreCase(MediaType.APPLICATION_JSON.getMediaTypeString())) {
                final NameValuePair charsetParam = elements[0].getParameterByName("charset");
                
                final Charset charset;
                if (charsetParam != null) {
                    charset = Charset.forName(charsetParam.getValue());
                } else {
                    charset = Charset.defaultCharset();
                }
    
                final String jsonText = new String(outputStream.toByteArray(), charset);
                
                if (jsonText.trim().length() > 0) {
                    return new JSONBody((JSONAware)JSONValue.parse(jsonText), charset);
                } else {
                    return new NoBody();
                }
            } else if (elements[0].getName().equalsIgnoreCase(MediaType.TEXT_PLAIN.getMediaTypeString())) {
                final NameValuePair charsetParam = elements[0].getParameterByName("charset");
                
                final Charset charset;
                if (charsetParam != null) {
                    charset = Charset.forName(charsetParam.getValue());
                } else {
                    charset = Charset.defaultCharset();
                }
    
                final String text = new String(outputStream.toByteArray(), charset);
                
                return new TextBody(text, charset);
            } else if (elements[0].getName().equalsIgnoreCase(MediaType.APPLICATION_XML.getMediaTypeString())) {
                final NameValuePair charsetParam = elements[0].getParameterByName("charset");
                
                final Charset charset;
                if (charsetParam != null) {
                    charset = Charset.forName(charsetParam.getValue());
                } else {
                    charset = Charset.defaultCharset();
                }
    
                final String text = new String(outputStream.toByteArray(), charset);
                
                return new TextBody(text, charset);            
            } else {
                return new BinaryBody(outputStream.toByteArray());
            }
        } catch (final IOException e) {
            return new NoBody();
        } finally {
            if (entityInputStream != null) {
                try {
                    entityInputStream.close();
                } catch (IOException e) {
                    logger.warn("Error closing entity input stream", e);
                }
            }
        }
    }
    
    public int getStatusCode() {
        return this.statusCode;
    }
    
    public String getStatusPhrase() {
        return this.statusPhrase;
    }
    
    public String getHeaderValue(final ResponseHeader header) {
        return getHeaderValue(header.getHeaderKeyString());
    }
    
    public String getHeaderValue(final String header) {
        return headerMap.get(header).getValue();
    }
    
    public IBody getBody() {
        return responseBody;
    }   
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(0xfff);
        
        builder.append("\nResponse\n");
        builder.append("********\n");
        builder.append("Status: " + statusCode + " - " + statusPhrase + "\n");
        builder.append("    (Request: " + request.getMethod() + " - " + request.getURI().toString() + ")\n\n");
        builder.append("Headers:\n");
        
        for (final Entry<String, Header> entry : headerMap.entrySet()) {
            builder.append("    " + entry.getKey() + " => " + entry.getValue().getValue() + "\n");
        }   

        builder.append("\n");
        builder.append("Body:\n");
        builder.append(responseBody.toString());
        builder.append("\n\n");
        
        return builder.toString();
    }
    
}
