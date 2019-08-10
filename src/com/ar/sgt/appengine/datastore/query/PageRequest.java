package com.ar.sgt.appengine.datastore.query;

public class PageRequest {

	private String cursor;
	
	private Integer pageSize;
	
	private Order order;
	
	private PageRequest(String cursor, Integer pageSize) {
		this.cursor = cursor;
		this.pageSize = pageSize;
	}
	
	public static PageRequest of(String cursor, Integer pageSize) {
		return new PageRequest(cursor, pageSize);
	}
	
	public String getCursor() {
		return cursor;
	}
	
	public Integer getPageSize() {
		return pageSize;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public PageRequest withOrder(Order order) {
		this.order = order;
		return this;
	}
	
	
}
