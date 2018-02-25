package ca.quadrilateral.integration.body;

import org.apache.http.HttpEntity;

public class NoBody implements IBody {
    public NoBody() {}

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("This method is not applicable for this implementation");
    }
    
    @Override
    public HttpEntity getHttpEntity() {
        return null;
    }
    
    @Override
    public String toString() {
        return "<NONE>";
    }
}
