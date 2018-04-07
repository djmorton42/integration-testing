package ca.quadrilateral.integration;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.integration.body.JSONBody;
import ca.quadrilateral.integration.builder.ITestDataSqlBuilder;

public class Integration implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Integration.class);

    private Random random = new Random();
    private Queue<HttpUriRequest> requestQueue = new ConcurrentLinkedDeque<>();
    private Map<HttpUriRequest, QueuedRequestResponseFuture> requestResponseMap = new ConcurrentHashMap<>();

    private final CloseableHttpClient defaultHttpClient;

    private final String appBasePath;

    public Integration(final String appBasePath) {
        defaultHttpClient = HttpClients.createDefault();
        this.appBasePath = appBasePath;
    }

    @Override
    public void close() {
        try {
            defaultHttpClient.close();
        } catch (final IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }

    public Response executeRequest(final RequestBuilder requestBuilder) throws RequestBuilderException {
        return executeRequest(requestBuilder.build());
    }

    public Response executeRequest(final RequestBuilder requestBuilder, final Integer statusToAssert) throws RequestBuilderException {
        return executeRequest(requestBuilder.build(), statusToAssert);
    }

    public Response executeRequest(final HttpUriRequest request) {
        return executeRequest(request, null);
    }

    public Response executeRequest(final HttpUriRequest request, final Integer statusToAssert) {
        logger.info(new HttpRequestLogStatementGenerator().toLogStatement(request));

        try (final CloseableHttpResponse httpResponse = defaultHttpClient.execute(request)) {
            final Response response = new Response(request, httpResponse);
            logger.info(response.toString());

            if (statusToAssert != null) {
                Assert.assertEquals("Incorrect status code returned in HTTP Request", (int)statusToAssert, response.getStatusCode());
            }

            return response;
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /*
    public Future<Response> queueRequest(final Request request) {
        final QueuedRequestResponseFuture future = new QueuedRequestResponseFuture(request);
        requestQueue.offer(request);
        requestResponseMap.put(request, future);
        return future;
    }

    public Future<Response> queueRequest(final RequestBuilder requestBuilder) throws RequestBuilderException {
        return queueRequest(requestBuilder.build());
    }

    public void clearRequestQueue() {
        requestQueue.clear();
        requestResponseMap.clear();
    }

    public void executeQueuedRequestsConcurrently() {
        executeQueuedRequestsConcurrently(ExecutionOrder.IN_ORDER);
    }

    public void executeQueuedRequestsConcurrently(final ExecutionOrder executionOrder) {
        executeQueuedRequestsConcurrently(executionOrder, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2), true);
    }

    public void executeQueuedRequestsConcurrently(final ExecutionOrder executionOrder, final int threads) {
        executeQueuedRequestsConcurrently(executionOrder, Executors.newFixedThreadPool(threads), true);
    }

    public void executeQueuedRequestsConcurrently(final ExecutionOrder executionOrder, final ExecutorService executor) {
        if (executionOrder == ExecutionOrder.IN_ORDER) {
            executeQueuedRequestsConcurrentlyInOrder(executor, false);
        } else if (executionOrder == ExecutionOrder.RANDOM) {
            executeQueuedRequestsConcurrentlyInRandomOrder(executor, false);
        } else {
            throw new AssertionError("Invalid ExecutionOrder: " + executionOrder);
        }
    }

    private void executeQueuedRequestsConcurrently(final ExecutionOrder executionOrder, final ExecutorService executor, final boolean shutdownExecutorWhenDone) {
        if (executionOrder == ExecutionOrder.IN_ORDER) {
            executeQueuedRequestsConcurrentlyInOrder(executor, shutdownExecutorWhenDone);
        } else if (executionOrder == ExecutionOrder.RANDOM) {
            executeQueuedRequestsConcurrentlyInRandomOrder(executor, shutdownExecutorWhenDone);
        } else {
            throw new AssertionError("Invalid ExecutionOrder: " + executionOrder);
        }
    }

    private void executeQueuedRequestsConcurrentlyInOrder(final ExecutorService executor, final boolean shutdownExecutorWhenDone) {
        final Queue<RequestAdapter> requestRunnableQueue = new ConcurrentLinkedDeque<>();

        while (!requestQueue.isEmpty()) {
            requestRunnableQueue.offer(new RequestAdapter(requestQueue.poll()));
        }

        executeRequestRunnablesConcurrently(executor, requestRunnableQueue, shutdownExecutorWhenDone);
    }

    private void executeQueuedRequestsConcurrentlyInRandomOrder(final ExecutorService executor, final boolean shutdownExecutorWhenDone) {
        final List<RequestAdapter> requestRunnableList = new ArrayList<>(requestQueue.size());
        while (!requestQueue.isEmpty()) {
            requestRunnableList.add(new RequestAdapter(requestQueue.poll()));
        }

        Collections.sort(requestRunnableList, new Comparator<RequestAdapter>() {

            @Override
            public int compare(final RequestAdapter o1, final RequestAdapter o2) {
                return random.nextInt(3) - 1;
            }

        });

        executeRequestRunnablesConcurrently(executor, requestRunnableList, shutdownExecutorWhenDone);
    }

    private void executeRequestRunnablesConcurrently(final ExecutorService executor, final Collection<RequestAdapter> requestTasks, final boolean shutdownExecutorWhenDone) {
        final CountDownLatch latch = new CountDownLatch(requestTasks.size());

        final List<Future<Response>> futures = new LinkedList<>();

        for (final RequestAdapter request : requestTasks) {
            request.setLatch(latch);

            final Future<Response> future = executor.submit(request);
            futures.add(future);
            requestResponseMap.get(request.request).setWrappedFuture(future);

            latch.countDown();
        }
        clearRequestQueue();
        if (shutdownExecutorWhenDone) {
            new Thread(new ExecutorShutdownTask(executor, futures)).start();
        }
    }

    public void executeQueuedRequestsSerially() {
        executeQueuedRequestsSerially(ExecutionOrder.IN_ORDER);
    }

    public void executeQueuedRequestsSerially(final ExecutionOrder executionOrder) {
        if (executionOrder == ExecutionOrder.IN_ORDER) {
            executeQueuedRequestsSeriallyInOrder();
        } else if (executionOrder == ExecutionOrder.RANDOM) {
            executeQueuedRequestsSeriallyInRandomOrder();
        } else {
            throw new AssertionError("Invalid ExecutionOrder: " + executionOrder);
        }
    }

    private void executeQueuedRequestsSeriallyInOrder() {
        final Queue<Request> requests = new ConcurrentLinkedDeque<>(requestQueue);
        executeRequestsSerially(requests);
    }

    private void executeQueuedRequestsSeriallyInRandomOrder() {
        final List<Request> requests = new ArrayList<>(requestQueue);
        Collections.sort(requests, new Comparator<Request>() {

            @Override
            public int compare(final Request o1, final Request o2) {
                return random.nextInt(3) - 1;
            }
        });

        executeRequestsSerially(requests);
    }

    private void executeRequestsSerially(final Collection<Request> requests) {
        for (final Request request : requests) {
            final Response response = executeRequest(request);
            requestResponseMap.get(request).setResponse(response);
        }
        clearRequestQueue();
    }

    private class RequestAdapter implements Callable<Response> {
        private final Request request;
        private CountDownLatch latch = null;

        public RequestAdapter(final Request request) {
            this.request = request;
        }

        public void setLatch(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Response call() throws Exception {
            if (latch == null) {
                return executeRequest(request);
            } else {
                latch.await();
                return executeRequest(request);
            }
        }
    }

    private static enum ExecutionOrder {
        IN_ORDER,
        RANDOM
    }

    private class ExecutorShutdownTask implements Runnable {
        private final ExecutorService executor;
        private final Collection<Future<Response>> futures;

        public ExecutorShutdownTask(final ExecutorService executor, final Collection<Future<Response>> futures) {
            this.executor = executor;
            this.futures = Collections.unmodifiableCollection(futures);
        }

        @Override
        public void run() {
            for (final Future<Response> future : futures) {
                try {
                    future.get();
                } catch (final ExecutionException | InterruptedException e) {
                    logger.warn("While waiting for futures to complete to shutdown executor, exception was thrown", e);
                }
            }
            executor.shutdownNow();
        }
    }
    */

    public void executeTestDataCreation(final ITestDataSqlBuilder... testDataItems) throws Exception {
        executeDatabaseCommands(
                Arrays
                    .stream(testDataItems)
                    .map(ITestDataSqlBuilder::buildSql)
                    .collect(Collectors.toList())
                );
    }

    public void executeDatabaseCommands(final String... commands) throws Exception {
        executeDatabaseCommands(Arrays.asList(commands));
    }

    public void executeDatabaseCommands(final List<String> commands) throws Exception {
        final String commandText = StringUtils.join(commands, ";");

        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.POST.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/data"))
                .addHeader(getHeader(RequestHeader.CONTENT_TYPE, MediaType.TEXT_PLAIN))
                .setEntity(new StringEntity(commandText, Charset.forName("UTF-8")))
                .build();

        final Response response = executeRequest(httpRequest);
        if (!response.isSuccess()) {
            throw new RuntimeException("Error executing database commands.  Successful status was expected.  Actual status was " + response.getStatusCode());
        }
    }

    public JSONObject executeSingleRowQuery(final String command) throws Exception {
        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.GET.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/data"))
                .addHeader(getHeader(RequestHeader.CONTENT_TYPE, MediaType.TEXT_PLAIN))
                .addHeader(getHeader(RequestHeader.ACCEPT, MediaType.APPLICATION_JSON))
                .setEntity(new StringEntity(command, Charset.forName("UTF-8")))
                .build();

        final Response response = executeRequest(httpRequest);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException(
                    "Error (" + response.getStatusCode() + " - " + response.getStatusPhrase() + ") " +
                    "executing single row databse query: " + command
            );
        }
        final JSONBody body = (JSONBody)response.getBody();

        final JSONArray rows = body.getJSONArray();
        if (rows.size() != 1) {
            throw new RuntimeException("Single row Database query must return one and only one row.  " + rows.size() + " rows were returned");
        }

        return (JSONObject)rows.get(0);
    }

    public JSONArray executeMultiRowQuery(final String command) throws Exception {
        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.GET.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/data"))
                .addHeader(getHeader(RequestHeader.CONTENT_TYPE, MediaType.TEXT_PLAIN))
                .addHeader(getHeader(RequestHeader.ACCEPT, MediaType.APPLICATION_JSON))
                .setEntity(new StringEntity(command, Charset.forName("UTF-8")))
                .build();

        final Response response = executeRequest(httpRequest);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException(
                    "Error (" + response.getStatusCode() + " - " + response.getStatusPhrase() + ") " +
                    "executing multi row databse query: " + command
            );
        }
        final JSONBody body = (JSONBody)response.getBody();

        return body.getJSONArray();
    }

    public Object executeScalarDatabaseRequest(final String command) throws Exception {
        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.GET.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/data"))
                .addHeader(getHeader(RequestHeader.CONTENT_TYPE, MediaType.TEXT_PLAIN))
                .addHeader(getHeader(RequestHeader.ACCEPT, MediaType.APPLICATION_JSON))
                .setEntity(new StringEntity(command, Charset.forName("UTF-8")))
                .build();

        final Response response = executeRequest(httpRequest);

        if (response.getStatusCode() != 200) {
        	throw new RuntimeException(
        			"Error (" + response.getStatusCode() + " - " + response.getStatusPhrase() + ") " +
        			"executing scalar databse request: " + command
        	);
        }
        final JSONBody body = (JSONBody)response.getBody();

        final JSONArray rows = body.getJSONArray();
        if (rows.size() != 1) {
          	throw new RuntimeException("Scalar Database Request must return one and only one row.  " + rows.size() + " rows were returned");
        }

        final JSONObject rowObject = (JSONObject)rows.get(0);

        if (rowObject.size() != 1) {
          	throw new RuntimeException("Scalar Database Request must return one and only one value.  " + rowObject.size() + " vlaues were returned");
        }

        return rowObject.get(rowObject.keySet().iterator().next());
    }

    public void clearDatabase() throws Exception {
        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.DELETE.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/data"))
                .build();

        executeRequest(httpRequest);
    }

    public void configureIntegrationSupport(final String configurationJson) throws Exception {
        HttpUriRequest httpRequest = RequestBuilder
                .create(HttpMethod.POST.toString())
                .setUri(getUriForAbsolutePath(getIntegrationBasePath() + "/configure"))
                .setEntity(new StringEntity(configurationJson, ContentType.APPLICATION_JSON.withCharset("UTF-8")))
                .build();

        executeRequest(httpRequest);
    }

    public static String getIntegrationBasePath() {
        return "/integration-test-support/integration";
    }

    public String getPathBase() {
        return appBasePath;
    }

    public static URIBuilder getUriBuilder() {
        return new URIBuilder()
            .setHost("localhost")
            .setPort(8080)
            .setScheme("http");
    }

    public URI getUri(final String path) throws URISyntaxException {
        return getUriBuilder().setPath(getPathBase() + path).build();
    }

    public URI getUriForAbsolutePath(final String absolutePath) throws URISyntaxException {
        return getUriBuilder().setPath(absolutePath).build();
    }

    public static <E extends Enum<E>> Header getHeader(final RequestHeader requestHeader, final E enumConstant) {
        return new BasicHeader(requestHeader.getHeaderKeyString(), enumConstant.toString());
    }

    public static Header getHeader(final RequestHeader requestHeader, final String value) {
        return new BasicHeader(requestHeader.getHeaderKeyString(), value);
    }



}
