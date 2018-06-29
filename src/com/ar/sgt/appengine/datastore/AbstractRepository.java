/*
Copyright (c) 2018, Sergio Gabriel Teves (https://github.com/dahool)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ar.sgt.appengine.datastore;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import com.ar.sgt.appengine.datastore.query.Order;
import com.ar.sgt.appengine.datastore.utils.EntityUtils;
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
	
	private TransactionManager txManager;
	
	@SuppressWarnings("unchecked")
	public AbstractRepository() {
		this.type = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepository.class);
		this.entityName = EntityMapper.getEntityType(type);
		this.txManager = new TransactionManager();
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
		save(obj, null);
	}

	@Override
	public void save(T obj, Transaction txn) {
		Entity entity = toEntity(obj);
		if (txn != null) {
			getDatastoreService().put(txn, entity);	
		} else {
			getDatastoreService().put(entity);
		}
		obj.setId(entity.getKey().getId());
	}

	@Override
	public void save(Iterable<T> it) {
		Transaction transaction = txManager.beginTransaction(true);
		for (T e : it) {
			save(e, transaction);
		}
		txManager.commit();
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
		query.addSort(EntityUtils.getFieldName(type, order.getFieldName()), order.getDirection());
		
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
	public void delete(Long id, Transaction txn) {
		if (id != null) {
			getDatastoreService().delete(txn, KeyFactory.createKey(entityName, id));
		}
	}
	
	@Override
	public void delete(Iterable<Long> idList) {
		Transaction transaction = txManager.beginTransaction(true);
		for (Long id : idList) {
			getDatastoreService().delete(transaction, KeyFactory.createKey(entityName, id));	
		}
		txManager.commit();
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
