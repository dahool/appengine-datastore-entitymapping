package com.ar.sgt.appengine.datastore;

import net.sf.cglib.proxy.InvocationHandler;
import java.lang.reflect.Method;

public abstract class LazyLoadHandler implements InvocationHandler  {

	private Object entity;
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (entity == null) {
			entity = loadEntity();
		}
		return method.invoke(entity, args);
	}

	protected abstract Object loadEntity();

}
