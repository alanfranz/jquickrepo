package eu.franzoni.jquickrepo.concurrency;

public interface WhileLocked<T> {
    public T execute();
}