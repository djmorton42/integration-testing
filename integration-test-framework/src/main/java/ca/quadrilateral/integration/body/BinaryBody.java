package ca.quadrilateral.integration.body;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import ca.quadrilateral.integration.MediaType;

public class BinaryBody implements IBody {
    private final byte[] bytes;
    private final MediaType mediaType;
    
    public BinaryBody(final byte[] bytes) {
        this.bytes = bytes.clone();
        this.mediaType = null;
    }
    
    public BinaryBody(final byte[] bytes, final MediaType mediaType) {
        this.bytes = bytes.clone();
        this.mediaType = mediaType;
    }
    
    @Override
    public byte[] getBytes() {
        return bytes.clone();
    }   

    @Override
    public HttpEntity getHttpEntity() {
        if (mediaType == null) {
            return new ByteArrayEntity(getBytes());
        } else {
            return new ByteArrayEntity(getBytes(), ContentType.create(mediaType.getMediaTypeString())); 
        }
    }
    
    @Override
    public String toString() {
        return new String(Hex.encodeHex(bytes));
    }   
}
