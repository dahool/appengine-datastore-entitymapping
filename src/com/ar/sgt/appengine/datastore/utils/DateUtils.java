package com.ar.sgt.appengine.datastore.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

	private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("America/Argentina/Buenos_Aires");
	private static final ZoneId ZONE_ID = TIME_ZONE.toZoneId();
	
	public static Date temporalToDate(Object value) {
		
		if (LocalDate.class.isAssignableFrom(value.getClass())) {
			LocalDate ld = (LocalDate) value;
			return Date.from(ld.atStartOfDay().atZone(ZONE_ID).toInstant());
		} else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			LocalDateTime ld = (LocalDateTime) value;
			return Date.from(ld.atZone(ZONE_ID).toInstant());
		} else if (LocalTime.class.isAssignableFrom(value.getClass())) {
			LocalTime ld = (LocalTime) value;
			return Date.from(ld.atDate(LocalDate.now()).atZone(ZONE_ID).toInstant());
		}
		return null;
	}

	public static Temporal dateToTemporal(Date value, Class<?> type) {
		if (LocalDate.class.isAssignableFrom(type)) {
			return value.toInstant().atZone(ZONE_ID).toLocalDate();
		} else if (LocalDateTime.class.isAssignableFrom(type)) {
			return value.toInstant().atZone(ZONE_ID).toLocalDateTime();
		} else if (LocalTime.class.isAssignableFrom(type)) {
			return value.toInstant().atZone(ZONE_ID).toLocalTime();
		}
		return null;
	}
	
}
