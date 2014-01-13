package eu.franzoni.jquickrepo.concurrency;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class ClosureLockTest extends TestCase {

    public class MockReadWriteLock implements ReadWriteLock {

        private Lock writeLock = new MockWriteLock();

        @Override
        public Lock readLock() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Lock writeLock() {
            return this.writeLock;
        }
    }

    public class MockWriteLock implements Lock {

        public boolean locked = false;
        public boolean unlocked = false;

        @Override
        public void lock() {
            this.locked = true;
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            throw new RuntimeException("must not be called");
        }

        @Override
        public boolean tryLock() {
            throw new RuntimeException("must not be called");
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new RuntimeException("must not be called");
        }

        @Override
        public void unlock() {
            this.unlocked = true;
        }

        @Override
        public Condition newCondition() {
            throw new RuntimeException("must not be called");
        }
    }

    // TODO: improve and split this basic test.
    @Test
    @SuppressWarnings("unchecked")
    public void testLockAndUnlockHappensAroundExecution() throws Exception {
        ReadWriteLock innerLock = new MockReadWriteLock();
        ScopedReadWriteLock lock = new ScopedReadWriteLock<Object>(innerLock);

        final Object obj = new Object();

        Object ret = lock.executeWithWriteLock(new WhileLocked<Object>() {
            @Override
            public Object execute() {
                return obj;
            }
        });

        final MockWriteLock writeLock = (MockWriteLock) innerLock.writeLock();

        Assert.assertTrue(obj == ret);
        Assert.assertTrue(writeLock.locked);
        Assert.assertTrue(writeLock.unlocked);


    }
}
