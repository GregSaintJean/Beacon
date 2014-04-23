package com.therabbitmage.android.beacon.utils;

import java.util.Collection;

public final class MiscUtils {
	
	public static <T> boolean isEmpty(Collection<T> collection){
		
		if(collection == null){
			return true;
		}
		
		return collection.isEmpty();
		
	}
	
	private MiscUtils(){}
}
