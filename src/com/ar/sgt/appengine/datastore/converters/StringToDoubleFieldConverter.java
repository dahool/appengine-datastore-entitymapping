package com.ar.sgt.appengine.datastore.converters;

public class StringToDoubleFieldConverter implements FieldConverter<String, Double> {

	@Override
	public boolean canConvert(Class<?> claz) {
		return String.class.isInstance(claz);
	}

	@Override
	public Double convert(String value) {
		return Double.valueOf(value);
	}
	
}
