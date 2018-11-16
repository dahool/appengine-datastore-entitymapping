package com.ar.sgt.appengine.datastore.converters;

public interface FieldConverter<S, T> {

	boolean canConvert(Class<?> claz);
	
	T convert(S value);
	
}
