package ca.quadrilateral.integration;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpRequestLogStatementGenerator {
    
    public String toLogStatement(final HttpUriRequest request) {
        final StringBuilder builder = new StringBuilder(0xfff);
        
        builder.append("\nRequest\n");
        builder.append("*******\n");
        builder.append(request.getMethod() + " Request to " + request.getURI().toString() + "\n\n");
        builder.append("Headers:\n");

        for (final Header header : request.getAllHeaders()) {
            builder.append("    " + header.getName() + " => " + header.getValue() + "\n");
        }
        
        builder.append("\n");
        builder.append("Body:\n");
        
        if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(request.getClass())) {
            final HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase)request;
            final HttpEntity entity = entityRequest.getEntity();
            if (entity != null) {

	            try {
	            	final String content = IOUtils.toString(entity.getContent());
	            	builder.append(content).append("\n\n");
	            } catch (final IOException e) {
	            	throw new RuntimeException(e);
	            }
            } else {
                builder.append("<NONE>\n\n");
            }
        } else {
            builder.append("<NONE>\n\n");
        }
        
        return builder.toString();
    }
    
}
