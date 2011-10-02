package eu.franzoni.quickrepo.concurrency;

import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class MultipleResourceLock {

    private ConcurrentMap<String, ReentrantReadWriteLock> locks =
            new MapMaker().weakValues().makeMap();

    public ReadWriteLock provideLock(final String id) {
        this.locks.putIfAbsent(id, new ReentrantReadWriteLock());
        return this.locks.get(id);
    }

}
