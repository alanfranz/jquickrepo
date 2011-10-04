package eu.franzoni.jquickrepo.repository;

public interface DoWhileLocking<T> {
    public T execute(T data);
}
