package eu.franzoni.jquickrepo.repository;

import java.util.Collection;

public interface Repository<T> {
    
    Collection<Entry<T>> loadAll();

    void delete(String id);

    T load(String id);

    void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking) throws UnknownResourceIdException;

    void modifyWhileLocking(final String id, final DoWhileLocking<T> whileLocking, final T missing) throws UnknownResourceIdException;

    void save(String id, T obj);

    void saveOrUpdate(String id, T obj);

    void update(String id, T obj);

}
