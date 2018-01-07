package org.duanbn.salix.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.duanbn.salix.network.exception.TimeoutException;
import org.duanbn.salix.network.message.AckMessage;

public class EndPointFuture<T extends AckMessage<?>> {

    private String                  requestId;

    private EndPointPromise<T>      promise;

    private boolean                 isSuccess;

    private List<FutureListener<T>> listeners;

    private Future<Void>            timeoutFuture;

    public EndPointFuture() {
        this.promise = new EndPointPromise<T>();
        this.listeners = new ArrayList<FutureListener<T>>();
    }

    public void addListener(FutureListener<T> listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void addListeners(FutureListener<T>... listeners) {
        if (listeners != null && listeners.length > 0) {
            this.listeners.addAll(Arrays.asList(listeners));
        }
    }

    public void await() throws InterruptedException {
        this.promise.doWait();
    }

    public void await(long millis) throws InterruptedException, TimeoutException {
        this.promise.doWait(millis);
    }

    public void signal() {
        this.promise.signal();
        FutureResult<T> futureResult = null;
        for (FutureListener<T> listener : this.listeners) {
            futureResult = new FutureResult<T>();

            futureResult.setSuccess(this.isSuccess);

            if (this.isSuccess) {
                futureResult.setAckMessage(this.promise.getValue());
            } else {
                futureResult.setCause(this.promise.getException());
            }

            listener.optionComplete(futureResult);
        }
    }

    public void setSuccess(T ackMessage) {
        this.isSuccess = true;
        this.promise.setValue(ackMessage);
    }

    public void setFailure(Throwable t) {
        this.isSuccess = false;
        this.promise.setException(t);
    }

    public T getMessage() {
        return this.promise.getValue();
    }

    public Throwable getThrowable() {
        return this.promise.getException();
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public Future<Void> getTimeoutFuture() {
        return timeoutFuture;
    }

    public void setTimeoutFuture(Future<Void> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
    }

    public static interface FutureListener<T extends AckMessage<?>> {

        void optionComplete(FutureResult<T> result);

    }

    public static class FutureResult<T extends AckMessage<?>> {

        private boolean   success;

        private T         ackMessage;

        private Throwable cause;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public T getAckMessage() {
            return ackMessage;
        }

        public void setAckMessage(T ackMessage) {
            this.ackMessage = ackMessage;
        }

        public Throwable getCause() {
            return cause;
        }

        public void setCause(Throwable cause) {
            this.cause = cause;
        }

    }
}
