package eu.franzoni.quickrepo.concurrency;

import junit.framework.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultipleResourceLockTest {
    final int LOCKS_COUNT = 100;
    final int THREAD_COUNT = 25;


    // TODO: this is an integration test, not a real unit test, change name and scope.
    @Test
    public void testLocksDontChoke() throws Exception {

        final MultipleResourceLock bigLock = new MultipleResourceLock();
        final Random generator = new Random();

        final Set<ReadWriteLock> locks = new HashSet<ReadWriteLock>();

        Collection<Callable<ReadWriteLock>> tasks = new ArrayList<Callable<ReadWriteLock>>();
        for (int i = 0; i < LOCKS_COUNT; i++) {

            // Tasks - each task makes exactly one service invocation.
            tasks.add(new Callable<ReadWriteLock>() {
                public ReadWriteLock call() throws Exception {
                    ReadWriteLock lock = bigLock.provideLock("sameid");

                    // verify it doesn't deadlock.
                    Lock writelock = lock.writeLock();
                    writelock.lock();
                    locks.add(lock);
                    Thread.sleep(generator.nextInt(50) + 1);
                    writelock.unlock();
                    return lock;
                }
            });
        }

        // Execute tasks
        //
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<ReadWriteLock>> futures = executorService.invokeAll(tasks); // invokeAll() blocks until all tasks have run.
        assertEquals(LOCKS_COUNT, futures.size());

        ReadWriteLock lastLock = locks.iterator().next();

        for (Future<ReadWriteLock> future : futures) {
            ReadWriteLock lock = future.get(); // get() will throw an exception if an exception was thrown by the service.
            assertTrue(lock != null); // did we get an article?
            // verify it's always the same lock.
            Assert.assertTrue(lock.equals(lastLock));
        }

    }

    // those tests may be stupid.
    @Test
    public void testLocksAreGarbageCollected() {
        MultipleResourceLock bigLock = new MultipleResourceLock();
        ReadWriteLock lock;

        lock = bigLock.provideLock("asd");
        int oldHash = System.identityHashCode(lock);
        lock = null;
        System.gc();
        lock = bigLock.provideLock("asd");
        int newHash = System.identityHashCode(lock);
        assertTrue(oldHash != newHash);


    }

    @Test
    public void testLocksAreReused() {
        MultipleResourceLock bigLock = new MultipleResourceLock();
        ReadWriteLock lock;

        lock = bigLock.provideLock("asd");
        int oldHash = System.identityHashCode(lock);
        System.gc();
        lock = bigLock.provideLock("asd");
        int newHash = System.identityHashCode(lock);
        assertTrue(oldHash == newHash);


    }

}
