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
