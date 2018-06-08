package com.ar.sgt.appengine.datastore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ar.sgt.appengine.datastore.annotation.EntityMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

public abstract class AbstractRepository<T extends AbstractEntity> implements Repository<T> {

	private final String entityName;
	
	private final Class<T> type;
	
	private final DatastoreService datastoreService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private EntityMapper mapper = new EntityMapper();
	
	public AbstractRepository(final Class<T> type) {
		this.type = type;
		this.entityName = type.getSimpleName();
		this.datastoreService = DatastoreServiceFactory.getDatastoreService();
	}
	
	protected T fromEntity(Entity entity) {
		try {
			return mapper.fromDatastoreEntity(entity, type, this.datastoreService);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("fromEntity: {}", e);
			throw new RuntimeException(e);
		}
	}

	protected Entity toEntity(T obj) {
		try {
			return mapper.toDatastoreEntity(obj);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("toEntity: {}", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Optional<T> get(Long id) {
		Entity entity;
		try {
			entity = datastoreService.get(KeyFactory.createKey(entityName, id));
		} catch (EntityNotFoundException e) {
			return Optional.ofNullable(null);
		}
		return Optional.of(fromEntity(entity));
	}

	@Override
	public void save(T obj) {
		Entity entity = toEntity(obj);
		datastoreService.put(entity);
		obj.setId(entity.getKey().getId());
	}

	@Override
	public void save(Iterable<T> it) {
		Transaction transaction = datastoreService.beginTransaction();
		for (T e : it) {
			save(e);
		}
		transaction.commit();
	}
	
	@Override
	public List<T> findAll() {
		Query query = new Query(entityName);
		PreparedQuery pq = datastoreService.prepare(query);
		
		List<T> objects = new ArrayList<T>();
		
		for (Entity entity : pq.asIterable()) {
			objects.add(fromEntity(entity));
		}
		
		return objects;
	}

	@Override
	public void delete(T obj) {
		if (obj != null && obj.getId() != null) {
			datastoreService.delete(KeyFactory.createKey(entityName, obj.getId()));
		}
	}
	
	@Override
	public void delete(Iterable<T> objList) {
		Transaction transaction = datastoreService.beginTransaction();
		for (T obj : objList) {
			datastoreService.delete(KeyFactory.createKey(entityName, obj.getId()));	
		}
		transaction.commit();
	}
	
	protected String getEntityName() {
		return entityName;
	}

	protected DatastoreService getDatastoreService() {
		return datastoreService;
	}
	
}
