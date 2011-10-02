package eu.franzoni.quickrepo.repository;

public interface DoWhileLocking<T> {
    public T execute(T data);
}
