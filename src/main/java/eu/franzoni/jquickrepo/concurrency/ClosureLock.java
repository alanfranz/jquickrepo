package eu.franzoni.jquickrepo.concurrency;


import java.util.concurrent.locks.Lock;

public class ClosureLock<T> {
    private Lock lock;

    public ClosureLock(Lock lock) {
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
