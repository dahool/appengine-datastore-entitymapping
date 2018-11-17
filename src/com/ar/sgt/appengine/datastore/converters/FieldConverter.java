package com.ar.sgt.appengine.datastore.converters;

public interface FieldConverter {

	Object fromDatastoreEntity(Object source);
	
	Object toDatastoreEntity(Object source);
	
}
