package ca.quadrilateral.integration.body;

import org.apache.http.HttpEntity;

public interface IBody {
    byte[] getBytes();

    HttpEntity getHttpEntity();
}
