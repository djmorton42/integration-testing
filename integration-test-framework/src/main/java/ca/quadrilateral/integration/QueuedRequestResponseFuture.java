package ca.quadrilateral.integration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueuedRequestResponseFuture implements Future<Response>{
    private final Request request;
    private volatile Future<Response> wrappedFuture;

    private volatile Response response = null;
    private volatile boolean isInterrupted = false;
    
    private boolean isCancelled = false;
    
    public QueuedRequestResponseFuture(final Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return this.request;
    }
    
    public void setResponse(final Response response) {
        this.response = response;
    }
    
    public void setWrappedFuture(final Future<Response> future) {
        this.wrappedFuture = future;
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (this.response != null) {
            return false;
        } else if (wrappedFuture != null) {
            return wrappedFuture.cancel(mayInterruptIfRunning);
        } else {
            isCancelled = true;
            if (mayInterruptIfRunning) {
                isInterrupted = true;
            }
            return true;
        }
    }

    @Override
    public boolean isCancelled() {
        if (isCancelled) {
            return true;
        } else if (wrappedFuture != null) {
            return wrappedFuture.isCancelled();
        } else {
            return false;
        }
    }

    @Override
    public boolean isDone() {
        if (isCancelled) {
            return true;
        } else if (wrappedFuture != null) {
            return wrappedFuture.isDone();
        } else {
            return response != null;
        }
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException {
        if (wrappedFuture != null) {
            return wrappedFuture.get();
        }   
        
        throwInterruptedExceptionIfNecessary();
        
        while (true) {
            try {
                return get(10, TimeUnit.SECONDS);
            } catch (final TimeoutException e) {
                //If it times out, we just keep trying, because we are
                //executing in the no timeout overload of get()
            }
        }
    }

    @Override
    public Response get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        throwInterruptedExceptionIfNecessary();
        
        if (wrappedFuture != null) {
            return wrappedFuture.get(timeout, unit);
        }
        
        while (System.currentTimeMillis() < endTime) {
            if (response != null) {
                return response;
            } else if (wrappedFuture != null) {
                return wrappedFuture.get(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }
            Thread.sleep(100);
            throwInterruptedExceptionIfNecessary();
        }
        
        throw new TimeoutException("Timeout expired trying to get result");
    }

    private void throwInterruptedExceptionIfNecessary() throws InterruptedException{
        if (Thread.currentThread().isInterrupted() || isInterrupted) {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Thread is interrupted!");
        }        
    }
}
