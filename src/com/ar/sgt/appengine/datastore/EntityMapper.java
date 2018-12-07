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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ar.sgt.appengine.datastore.annotation.Converter;
import com.ar.sgt.appengine.datastore.annotation.EntityName;
import com.ar.sgt.appengine.datastore.annotation.Id;
import com.ar.sgt.appengine.datastore.annotation.Lazy;
import com.ar.sgt.appengine.datastore.annotation.Unindexed;
import com.ar.sgt.appengine.datastore.converters.FieldConverter;
import com.ar.sgt.appengine.datastore.utils.DateUtils;
import com.ar.sgt.appengine.datastore.utils.EntityUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import net.sf.cglib.proxy.Enhancer;

public class EntityMapper {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, List<Field>> cached = new HashMap<String, List<Field>>();
	
	private static final String BOUND_FIELD = "CGLIB$";
	
	private Map<Class<?>, FieldConverter> convertersCache = new HashMap<>();
	
	public com.google.appengine.api.datastore.Entity toDatastoreEntity(AbstractEntity element, Class<?> type) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException  {

		if (element == null) return null;
		
		String entityName = getEntityName(type);
		
		com.google.appengine.api.datastore.Entity entity = element.getId() == null ? new com.google.appengine.api.datastore.Entity(entityName) : new com.google.appengine.api.datastore.Entity(KeyFactory.createKey(entityName, element.getId()));
		
		for (Field field : getFields(element.getClass())) {
			if (field.isAnnotationPresent(Id.class)) {
				continue;
			} else if (field.getName().startsWith(BOUND_FIELD)) {
				continue;
			}
			String fieldName = EntityUtils.getFieldName(field);
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
				String fieldName = EntityUtils.getFieldName(field);
				Object value = entity.getProperty(fieldName);
				if (value != null && AbstractEntity.class.isAssignableFrom(field.getType())) {
					Object fk = null;
					if (field.isAnnotationPresent(Lazy.class)) {
						fk = getLazyRelated(field.getType(), datastoreService, (Long) value);
					} else {
						fk = getRelated(field.getType(), datastoreService, (Long) value);
					}
					setPropertyValue(field, fk, newObject);	
				} else {
					setPropertyValue(field, value, newObject);	
				}
			}
		}

		return newObject;
	}
	
	private Object getLazyRelated(final Class<?> type, final DatastoreService datastoreService, final Long value) {
		Object lazyEntity = Enhancer.create(type, new LazyLoadHandler() {
			@Override
			protected Object loadEntity() {
				try {
					logger.debug("Lazy loading {} with id {}", type, value);
					return getRelated(type, datastoreService, value);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
						| InstantiationException e) {
					logger.error("{}", e);
				}
				return null;
			}
		});
		return lazyEntity;
	}

	private Object getRelated(final Class<?> type, final DatastoreService datastoreService, final Long id) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Entity entity;
		try {
			entity = datastoreService.get(KeyFactory.createKey(getEntityName(type), id));
		} catch (EntityNotFoundException e) {
			return null;
		}
		return fromDatastoreEntity(entity, type, datastoreService);
	}

	private void setPropertyValue(final Field field, final Object value, final Object element) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		if (value != null && field.isAnnotationPresent(Converter.class)) {
			Converter ca = field.getAnnotation(Converter.class);
			FieldConverter fc = getConverterInstance(ca.value());
			PropertyUtils.setProperty(element, field.getName(), fc.fromDatastoreEntity(value));
		} else if (value != null && Temporal.class.isAssignableFrom(field.getType())) {
			PropertyUtils.setProperty(element, field.getName(), DateUtils.dateToTemporal((Date) value, field.getType()));
		} else if (value != null && com.google.appengine.api.datastore.Text.class.isAssignableFrom(value.getClass())) {
			Text text = (Text) value;
			PropertyUtils.setProperty(element, field.getName(), text.getValue());
		} else {
			PropertyUtils.setProperty(element, field.getName(), value);	
		}
	}
	
	private Object getPropertyValue(final Field field, final Object element) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Object value = PropertyUtils.getProperty(element, field.getName());
		if (value == null) return null;
		
		if (field.isAnnotationPresent(Converter.class)) {
			Converter ca = field.getAnnotation(Converter.class);
			FieldConverter fc = getConverterInstance(ca.value());
			return fc.toDatastoreEntity(value);
		} else if (AbstractEntity.class.isAssignableFrom(value.getClass())) {
			 return ((AbstractEntity) value).getId();
		} else if (Temporal.class.isAssignableFrom(value.getClass()) ) {
			return DateUtils.temporalToDate(value);
		}
		return value;
		
	}
	
	private List<Field> getFields(final Class<?> type) {
		
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

	
	public static String getEntityName(final Class<?> type) {
		if (type.isAnnotationPresent(EntityName.class)) {
			EntityName nm = type.getAnnotation(EntityName.class);
			return nm.value();
		}
		return type.getSimpleName();	
	}

	
	private FieldConverter getConverterInstance(Class<? extends FieldConverter> class1) throws InstantiationException, IllegalAccessException {
		if (convertersCache.containsKey(class1)) {
			return convertersCache.get(class1);
		}
		FieldConverter cInstance = class1.newInstance();
		convertersCache.put(class1, cInstance);
		return cInstance;
	}
	
}
