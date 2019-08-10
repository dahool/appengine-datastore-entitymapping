package com.ar.sgt.appengine.datastore.query;

public class PageResult<T> {

	private Iterable<T> result;
	
	private String cursor;
	
	public PageResult(Iterable<T> result, String cursor) {
		this.result = result;
		this.cursor = cursor;
	}
	
	public String getCursor() {
		return cursor;
	}
	
	public Iterable<T> getResult() {
		return result;
	}
	
}
