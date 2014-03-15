package com.therabbitmage.android.beacon.utils;

import java.text.DateFormat;
import java.util.Calendar;

public final class TimeUtils {
	
	public static final String getCurrentDate(){
		Calendar now = Calendar.getInstance();
		DateFormat df = DateFormat.getDateInstance();
		return df.format(new java.util.Date(now.getTimeInMillis()));
	}
	
	public static final String getCurrentTime(){
		Calendar now = Calendar.getInstance();
		DateFormat df = DateFormat.getTimeInstance();
		return df.format(new java.util.Date(now.getTimeInMillis()));
	}
	
	public static final String getCurrentDateTime(){
		Calendar now = Calendar.getInstance();
		DateFormat df = DateFormat.getDateTimeInstance();
		return df.format(new java.util.Date(now.getTimeInMillis()));
	}
	
	public static final String getSQLFormattedTime(){
		Calendar now = Calendar.getInstance();
		java.sql.Date date = new java.sql.Date(now.getTimeInMillis());
		return date.toString();
	}
	
	private TimeUtils(){}

}
