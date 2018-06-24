package com.ar.sgt.appengine.datastore;

import java.util.List;
import java.util.Optional;

import com.ar.sgt.appengine.datastore.query.Order;

public interface Repository<T extends AbstractEntity> {

	Optional<T> get(Long id);

	void save(T obj);

	List<T> findAll();

	List<T> findAll(Order order);
	
	void delete(Long id);

	void delete(Iterable<Long> it);

	void save(Iterable<T> it);

}