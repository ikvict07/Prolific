package org.nevertouchgrass.prolific.repository;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface BasicRepository<T> {
    void save(T t);

    void saveAll(Iterable<T> t);

    Iterable<T> findAll(Class<T> clazz);

    T findById(Long id, Class<T> clazz);

    T update(T t);

}
