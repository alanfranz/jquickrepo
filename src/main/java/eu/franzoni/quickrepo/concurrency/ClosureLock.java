package eu.franzoni.quickrepo.concurrency;


import java.util.concurrent.locks.Lock;

public class ClosureLock {
    private Lock lock;

    public ClosureLock(Lock lock) {
        this.lock = lock;
    }

    public void executeWhileLocking(WhileLocked whileLocked) {
        this.lock.lock();

        try {
            whileLocked.execute();
        } finally {
            this.lock.unlock();
        }

    }


}
