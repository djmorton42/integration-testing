package ca.quadrilateral.integration.body;

import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

public class TextBody implements IBody {
    private final String text;
    private final Charset encoding;
    
    public TextBody(final String text, final Charset encoding) {
        this.text = text;
        this.encoding = encoding;
    }
    
    @Override
    public byte[] getBytes() {
        return text.getBytes(encoding);
    }

    @Override
    public HttpEntity getHttpEntity() {
        return new StringEntity(text, encoding);
    }
    
    @Override
    public String toString() {
        return text;
    }
}

