package eu.franzoni.jquickrepo.concurrency;


import java.util.concurrent.locks.Lock;

public class ScopedLock<T> {
    private Lock lock;

    public ScopedLock(Lock lock) {
        this.lock = lock;
    }

    public T executeWhileLocking(WhileLocked<T> whileLocked) {
        this.lock.lock();

        try {
            return whileLocked.execute();
        } finally {
            this.lock.unlock();
        }

    }


}
