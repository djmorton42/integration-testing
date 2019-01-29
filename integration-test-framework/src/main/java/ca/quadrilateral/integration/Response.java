package ca.quadrilateral.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

    private static final Pattern LOCATION_ID_PATTERN = Pattern.compile("^.*/([0-9]+$)");

    public Response(final HttpUriRequest request, final HttpResponse httpResponse) {
        this.request = request;
        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.statusPhrase = httpResponse.getStatusLine().getReasonPhrase();

        final Header[] allHeaders = httpResponse.getAllHeaders();
        for (final Header header : allHeaders) {
            headerMap.put(header.getName().toLowerCase(), header);
        }

        this.responseBody = buildResponseBody(httpResponse);
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }

    public boolean isUserError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isSystemError() {
        return statusCode >= 500 && statusCode < 600;
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
        return headerMap.get(header.toLowerCase()).getValue();
    }

    public Long extractLocationHeaderId() {
        final String headerValue = getHeaderValue(ResponseHeader.LOCATION);
        if (StringUtils.isBlank(headerValue)) {
            throw new IllegalStateException("Location header not present");
        }

        final Matcher locationIdMatcher = LOCATION_ID_PATTERN.matcher(headerValue);
        if (!locationIdMatcher.matches()) {
            throw new IllegalStateException("Could not extract location header id from '" + headerValue + "'");
        } else {
            return Long.parseLong(locationIdMatcher.group(1));
        }
    }

    public IBody getBody() {
        return responseBody;
    }

    public boolean hasBody() {
        return !(responseBody instanceof NoBody);
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
