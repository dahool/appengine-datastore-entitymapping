package com.ar.sgt.appengine.datastore.query;

import com.google.appengine.api.datastore.Query.SortDirection;

public class Order {

	private String fieldName;
	
	private SortDirection direction;
	
	private Order(String field, SortDirection direction) {
		this.fieldName = field;
		this.direction = direction;
	}

	public static Order of(final String field) {
		return new Order(field, SortDirection.ASCENDING);
	}
	
	public static Order of(final String field, final SortDirection direction) {
		return new Order(field, direction);
	}

	public String getFieldName() {
		return fieldName;
	}

	public SortDirection getDirection() {
		return direction;
	}

}
