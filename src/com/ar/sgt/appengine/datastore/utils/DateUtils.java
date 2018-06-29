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
