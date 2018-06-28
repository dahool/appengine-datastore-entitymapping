package com.ar.sgt.appengine.datastore.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ar.sgt.appengine.datastore.annotation.FieldName;

public class EntityUtils {

	private static final Map<String, String> fieldCache = Collections.synchronizedMap(new HashMap<String, String>());
	
	public static String getFieldName(Field field) {
		String name = fieldCache.get(cacheKey(field));
		if (name == null) {
			if (field.isAnnotationPresent(FieldName.class)) {
				FieldName fn = field.getAnnotation(FieldName.class);
				name = fn.value();
			} else {
				name = field.getName();	
			}
			fieldCache.put(cacheKey(field), name);
		}
		return name;
	}

	public static String getFieldName(Class<?> type, String field) {
		try {
			return getFieldName(type.getDeclaredField(field));
		} catch (NoSuchFieldException | SecurityException e) {
			return field;
		}
	}
	
	private static String cacheKey(Field field) {
		return String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
	}
	
}
