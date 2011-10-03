package eu.franzoni.quickrepo.concurrency;

public interface WhileLocked<T> {
    public T execute();
}