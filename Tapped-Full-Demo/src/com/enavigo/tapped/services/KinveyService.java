package com.enavigo.tapped.services;

import com.enavigo.tapped.TappedApplication;
import com.enavigo.tapped.entities.BeamReceivedEntity;
import com.enavigo.tapped.entities.TagReadEntity;
import com.enavigo.tapped.utils.Constants;
import com.kinvey.util.ScalarCallback;

public class KinveyService {
	
	public static void saveBeam(TappedApplication context, BeamReceivedEntity entity, ScalarCallback callback){
		context.getKinveyService().mappeddata(Constants.KINVEY_COLLECTION_BEAMS).save(entity, callback);
	}
	
	public static void saveTag(TappedApplication context, TagReadEntity entity, ScalarCallback callback){
		context.getKinveyService().mappeddata(Constants.KINVEY_COLLECTION_TAGS).save(entity, callback);
	}

}
