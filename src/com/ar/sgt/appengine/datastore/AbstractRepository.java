package com.ar.sgt.appengine.datastore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import com.ar.sgt.appengine.datastore.query.Order;
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
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private EntityMapper mapper = new EntityMapper();
	
	private DatastoreService datastoreService;
	
	@SuppressWarnings("unchecked")
	public AbstractRepository() {
		this.type = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepository.class);
		this.entityName = EntityMapper.getEntityType(type);
	}
	
	protected T fromEntity(Entity entity) {
		try {
			return mapper.fromDatastoreEntity(entity, type, getDatastoreService());
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
			entity = getDatastoreService().get(KeyFactory.createKey(entityName, id));
		} catch (EntityNotFoundException e) {
			return Optional.ofNullable(null);
		}
		return Optional.of(fromEntity(entity));
	}

	@Override
	public void save(T obj) {
		Entity entity = toEntity(obj);
		getDatastoreService().put(entity);
		obj.setId(entity.getKey().getId());
	}

	@Override
	public void save(Iterable<T> it) {
		Transaction transaction = getDatastoreService().beginTransaction();
		for (T e : it) {
			save(e);
		}
		transaction.commit();
	}
	
	@Override
	public List<T> findAll() {
		Query query = new Query(entityName);

		PreparedQuery pq = getDatastoreService().prepare(query);

		List<T> objects = new ArrayList<T>();
		
		for (Entity entity : pq.asIterable()) {
			objects.add(fromEntity(entity));
		}
		
		return objects;
	}

	@Override
	public List<T> findAll(Order order) {
		Query query = new Query(entityName);
		query.addSort(order.getFieldName(), order.getDirection());
		
		PreparedQuery pq = getDatastoreService().prepare(query);

		List<T> objects = new ArrayList<T>();
		
		for (Entity entity : pq.asIterable()) {
			objects.add(fromEntity(entity));
		}
		
		return objects;

	}
	
	@Override
	public void delete(Long id) {
		if (id != null) {
			getDatastoreService().delete(KeyFactory.createKey(entityName, id));
		}
	}
	
	@Override
	public void delete(Iterable<Long> idList) {
		Transaction transaction = getDatastoreService().beginTransaction();
		for (Long id : idList) {
			getDatastoreService().delete(KeyFactory.createKey(entityName, id));	
		}
		transaction.commit();
	}
	
	protected String getEntityName() {
		return entityName;
	}

	protected void initializeDatastore() {
		synchronized (this) {
			if (this.datastoreService == null) {
				this.datastoreService = DatastoreServiceFactory.getDatastoreService();
			}
		}
	}
	protected DatastoreService getDatastoreService() {
		if (this.datastoreService == null) {
			this.initializeDatastore();
		}
		return this.datastoreService;
	}
	
}
