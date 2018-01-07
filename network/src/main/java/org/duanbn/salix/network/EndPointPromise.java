package org.duanbn.salix.network;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.duanbn.salix.network.exception.TimeoutException;

/**
 * one promise object will be never wait when has signaled
 * 
 * @author shanwei Dec 7, 2017 4:54:06 PM
 */
public class EndPointPromise<T> {

    private Lock          lock      = new ReentrantLock();

    private Condition     wait      = lock.newCondition();

    private AtomicBoolean hasSignal = new AtomicBoolean();

    private T             value;

    private Throwable     exception;

    public void doWaitUninterruptibly() {
        this.lock.lock();
        try {
            if (this.hasSignal.get()) {
                return;
            }

            this.wait.awaitUninterruptibly();
        } finally {
            this.lock.unlock();
        }
    }

    public void doWait() throws InterruptedException {
        this.lock.lock();
        try {
            if (this.hasSignal.get()) {
                return;
            }

            this.wait.await();
        } finally {
            this.lock.unlock();
        }
    }

    public void doWait(long millis) throws InterruptedException, TimeoutException {
        this.lock.lock();
        try {
            if (this.hasSignal.get()) {
                return;
            }

            boolean result = this.wait.await(millis, TimeUnit.MILLISECONDS);

            if (!result) {
                throw new TimeoutException("wait timeout");
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void signal() {
        this.lock.lock();
        try {
            if (this.hasSignal.get()) {
                return;
            }

            this.hasSignal.set(true);

            this.wait.signal();
        } finally {
            this.lock.unlock();
        }
    }

    public void signalAll() {
        this.lock.lock();
        try {
            if (this.hasSignal.get()) {
                return;
            }

            this.hasSignal.set(true);

            this.wait.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return this.exception;
    }

    public boolean isSuccess() {
        return this.exception == null;
    }

}
