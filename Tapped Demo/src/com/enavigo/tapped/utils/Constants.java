package com.enavigo.tapped.utils;

import java.util.HashMap;
import java.util.HashSet;

public class Constants {
	// Application mime type
	public static final String MIME_TYPE = "application/com.enavigo.tapped";
	
	// For Kinvey - see TappedApplication for usage
	public static final String KINVEY_KEY = "kid_VPBToYGel";
	public static final String KINVEY_SECRET_KEY = "ed9e2e51121f442694ef74709513ed80";
	
	// Kinvey collections
	public static final String KINVEY_COLLECTION_BEAMS = "beams";
	public static final String KINVEY_COLLECTION_TAGS = "tags";
	
	// Places
	public static final String PLACE_NYC = "New York City";
	public static final String PLACE_SAN_FRAN = "San Francisco";
	public static final String PLACE_BOSTON = "Boston";
	
	public static final HashSet<String> VALID_PLACES = new HashSet<String>() {
		{
			add(PLACE_SAN_FRAN);
			add(PLACE_NYC);
			add(PLACE_BOSTON);
		}
	};

	// IDs of the event places on Facebook
	public static final String FB_PLACE_ID_NYC = "119216568127407";
	public static final String FB_PLACE_ID_SF = "154783721256942";
	public static final String FB_PLACE_ID_BOSTON = "76530400930";
	
	public static final HashMap<String,String> PLACE_TO_FB_ID = new HashMap<String,String>(){
		{
			put(PLACE_NYC, FB_PLACE_ID_NYC);
			put(PLACE_SAN_FRAN, FB_PLACE_ID_SF);
			put(PLACE_BOSTON, FB_PLACE_ID_BOSTON);
		}
	};
}
