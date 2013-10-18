package eu.franzoni.jquickrepo.concurrency;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ClosureLockTest extends TestCase {
    public class MockLock implements Lock {
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
    public void testLockAndUnlockHappensAroundExecution() throws Exception {
        MockLock innerLock = new MockLock();
        ScopedLock lock = new ScopedLock<Object>(innerLock);
        
        final Object obj = new Object();

        Object ret = lock.executeWhileLocking(new WhileLocked<Object>() {
            @Override
            public Object execute() {
                return obj;
            }
        });

        Assert.assertTrue(obj == ret);
        Assert.assertTrue(innerLock.locked);
        Assert.assertTrue(innerLock.unlocked);


    }
}
