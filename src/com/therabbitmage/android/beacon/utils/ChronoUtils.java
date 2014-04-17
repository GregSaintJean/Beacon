package com.therabbitmage.android.beacon.utils;

import java.text.DateFormat;
import java.util.Calendar;

public class ChronoUtils{

	public static final long MILLISECONDS_PER_SECOND = 1000;
	
	private static final long SECONDS_IN_ONE_MINUTE =  60;
	private static final long MINUTES_IN_ONE_HOUR = 60;
	private static final long HOURS_IN_ONE_DAY =  24;
	private static final long DAYS_IN_ONE_MONTH = 30;
	private static final long DAYS_IN_ONE_MONTH_2 = 31;
	private static final long DAYS_IN_FEBRUARY = 29;
	private static final long MONTHS_IN_YEAR = 12;
	
	public static final long ONE_SECOND = MILLISECONDS_PER_SECOND;
	
	public static final long ONE_MINUTE = ONE_SECOND * SECONDS_IN_ONE_MINUTE;
	
	public static final long ONE_HOUR =  ONE_MINUTE * MINUTES_IN_ONE_HOUR;
	
	public static final long ONE_DAY = ONE_HOUR * HOURS_IN_ONE_DAY;
	
	public static final long ONE_MONTH =  ONE_DAY * DAYS_IN_ONE_MONTH;
	
	public static final long ONE_MONTH_2 =  ONE_DAY * DAYS_IN_ONE_MONTH_2;
	
	public static final long FEBRUARY_DAYS = ONE_DAY * DAYS_IN_FEBRUARY;
	
	public static final long ONE_YEAR = ONE_MONTH * MONTHS_IN_YEAR;
	
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
	
	public static final String getCurrentSQLFormattedTime(){
		Calendar now = Calendar.getInstance();
		java.sql.Date date = new java.sql.Date(now.getTimeInMillis());
		return date.toString();
	}
	
	private ChronoUtils(){}

}
