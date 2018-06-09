package com.ar.sgt.appengine.datastore.utils;

import java.lang.reflect.Field;

import com.ar.sgt.appengine.datastore.annotation.FieldName;

public class EntityUtils {

	public static String getFieldName(Field field) {
		if (field.isAnnotationPresent(FieldName.class)) {
			FieldName fn = field.getAnnotation(FieldName.class);
			return fn.value();
		}
		return field.getName();
	}

	public static String getFieldName(Class<?> type, String field) {
		try {
			return getFieldName(type.getDeclaredField(field));
		} catch (NoSuchFieldException | SecurityException e) {
			return field;
		}
	}
	
}
