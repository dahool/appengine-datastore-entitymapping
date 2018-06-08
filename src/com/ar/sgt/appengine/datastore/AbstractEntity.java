package com.ar.sgt.appengine.datastore;

import java.io.Serializable;

import com.ar.sgt.appengine.datastore.annotation.Id;

@SuppressWarnings("serial")
public abstract class AbstractEntity implements Serializable {

	@Id
	private Long id;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long key) {
		this.id = key;
	}
	
}
