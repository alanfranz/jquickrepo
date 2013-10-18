package eu.franzoni.jquickrepo.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class ScopedReadWriteLock<T> {

    private final ReadWriteLock readWriteLock;

    public ScopedReadWriteLock(java.util.concurrent.locks.ReadWriteLock lock) {
        this.readWriteLock = lock;
    }

    public T executeWithWriteLock(WhileLocked<T> whileLocked) {
        final Lock writeLock = this.readWriteLock.writeLock();
        writeLock.lock();

        try {
            return whileLocked.execute();
        } finally {
            writeLock.unlock();
        }

    }

    public T executeWithReadLock(WhileLocked<T> whileLocked) {
        final Lock readLock = this.readWriteLock.readLock();
        readLock.lock();

        try {
            return whileLocked.execute();
        } finally {
            readLock.unlock();
        }

    }
}
