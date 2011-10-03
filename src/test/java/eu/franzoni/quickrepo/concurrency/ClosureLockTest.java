package eu.franzoni.quickrepo.concurrency;

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

    // TODO: improve this basic test.
    @Test
    public void testLockAndUnlockHappensAroundExecution() throws Exception {
        MockLock innerLock = new MockLock();
        ClosureLock lock = new ClosureLock(innerLock);

        lock.executeWhileLocking(new WhileLocked() {
            @Override
            public void execute() {
                            }
        });

        Assert.assertTrue(innerLock.locked);
        Assert.assertTrue(innerLock.unlocked);

        
    }
}
