package com.ar.sgt.appengine.datastore;

import java.util.List;
import java.util.Optional;

public interface Repository<T extends AbstractEntity> {

	Optional<T> get(Long id);

	void save(T obj);

	List<T> findAll();

	void delete(T obj);

	void delete(Iterable<T> it);

	void save(Iterable<T> it);

}