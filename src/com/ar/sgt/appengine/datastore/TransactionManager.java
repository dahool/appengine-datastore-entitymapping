package com.ar.sgt.appengine.datastore;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;

public class TransactionManager {

	private Transaction transaction = null;
	
	public void beginTransaction() {
		if (transaction != null) {
			throw new RuntimeException("A transaction already exists");
		}
		transaction = DatastoreServiceFactory.getDatastoreService().beginTransaction();
	}
	
	public void commit() {
		if (transaction != null) transaction.commit();
		transaction = null;
	}
	
	public void rollback() {
		if (transaction != null) transaction.rollback();
		transaction = null;
	}
	
}
