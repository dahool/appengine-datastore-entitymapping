package com.ar.sgt.appengine.datastore.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.ar.sgt.appengine.datastore.AbstractEntity;
import com.ar.sgt.appengine.datastore.utils.DateUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class EntityMapper {

	private Map<String, List<Field>> cached = new HashMap<String, List<Field>>();
	
	public com.google.appengine.api.datastore.Entity toDatastoreEntity(AbstractEntity element) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException  {

		if (element == null) return null;
		
		String entityType = getEntityType(element.getClass());
		
		com.google.appengine.api.datastore.Entity entity = element.getId() == null ? new com.google.appengine.api.datastore.Entity(entityType) : new com.google.appengine.api.datastore.Entity(KeyFactory.createKey(entityType, element.getId()));
		
		for (Field field : getFields(element.getClass())) {
			if (field.isAnnotationPresent(Id.class)) {
				continue;
			}
			String fieldName = field.getName();
			if (field.isAnnotationPresent(FieldName.class)) {
				FieldName fn = field.getAnnotation(FieldName.class);
				fieldName = fn.value();
			}
			Object value = getPropertyValue(field, element);
			if (field.isAnnotationPresent(Unindexed.class)) {
				entity.setUnindexedProperty(fieldName, value);				
			} else {
				entity.setProperty(fieldName, value);
			}
		}
		
		return entity;
		
	}
	
	public <T> T fromDatastoreEntity(com.google.appengine.api.datastore.Entity entity, Class<T> type, DatastoreService datastoreService) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException  {
		
		if (entity == null) return null;
		
		T newObject = type.newInstance();
		
		for (Field field : getFields(type)) {
			if (field.isAnnotationPresent(Id.class)) {
				setPropertyValue(field, entity.getKey().getId(), newObject);
			} else {
				String fieldName = field.getName();
				if (field.isAnnotationPresent(FieldName.class)) {
					FieldName fn = field.getAnnotation(FieldName.class);
					fieldName = fn.value();
				}
				Object value = entity.getProperty(fieldName);
				if (value != null && AbstractEntity.class.isAssignableFrom(field.getType())) {
					Object fk = getRelated(field.getType(), datastoreService, (Long) value);
					setPropertyValue(field, fk, newObject);	
				} else {
					setPropertyValue(field, value, newObject);	
				}
			}
		}

		return newObject;
	}
	
	private Object getRelated(Class<?> type, DatastoreService datastoreService, Long id) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Entity entity;
		try {
			entity = datastoreService.get(KeyFactory.createKey(getEntityType(type), id));
		} catch (EntityNotFoundException e) {
			return null;
		}
		return fromDatastoreEntity(entity, type, datastoreService);
	}

	private void setPropertyValue(Field field, Object value, Object element) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (value != null && Temporal.class.isAssignableFrom(field.getType())) {
			PropertyUtils.setProperty(element, field.getName(), DateUtils.dateToTemporal((Date) value, field.getType()));
		} else {
			PropertyUtils.setProperty(element, field.getName(), value);	
		}
	}
	
	private Object getPropertyValue(Field field, Object element) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object value = PropertyUtils.getProperty(element, field.getName());
		if (value != null && AbstractEntity.class.isAssignableFrom(value.getClass())) {
			 return ((AbstractEntity) value).getId();
		} else if (value != null && Temporal.class.isAssignableFrom(value.getClass()) ) {
			return DateUtils.temporalToDate(value);
		}
		return value;
	}
	
	private List<Field> getFields(Class<?> type) {
		
		if (cached.get(type.getName()) != null) return cached.get(type.getName());
		
		List<Field> fields = new ArrayList<Field>();
		
		if (type.getSuperclass() != null) {
			fields.addAll(getFields(type.getSuperclass()));
		}
		
		for (Field field : type.getDeclaredFields()) {
			if (isAccesible(field.getModifiers())) {
				field.setAccessible(true);
				fields.add(field);
			}
		}
		
		cached.put(type.getName(), fields);
		
		return fields;
	}
	
	private boolean isAccesible(int modifier) {
		return !(Modifier.isTransient(modifier) || Modifier.isStatic(modifier) || Modifier.isAbstract(modifier));
	}

	
	public static String getEntityType(Class<?> type) {
		if (type.isAnnotationPresent(EntityName.class)) {
			EntityName nm = type.getAnnotation(EntityName.class);
			return nm.value();
		}
		return type.getSimpleName();	
	}
	
}
