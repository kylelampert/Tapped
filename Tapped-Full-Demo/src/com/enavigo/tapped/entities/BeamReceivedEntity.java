package com.enavigo.tapped.entities;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class BeamReceivedEntity implements MappedEntity {

	private String id;
	private String userName;
	private String beamMessage;
	private Long beamTime;
	
	public BeamReceivedEntity(){}
	
	public BeamReceivedEntity(String userName, String beamMessage, long beamTime){
		this.userName = userName;
		this.beamMessage = beamMessage;
		this.beamTime = beamTime;
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] { 
				new MappedField("id", "_id"),
				new MappedField("userName", "userName"),
				new MappedField("beamMessage", "beamMessage"), 
				new MappedField("beamTime", "beamTime") });
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getBeamMessage() {
		return beamMessage;
	}
	public void setBeamMessage(String beamMessage) {
		this.beamMessage = beamMessage;
	}

	public Long getBeamTime() {
		return beamTime;
	}
	public void setBeamTime(Long beamTime) {
		this.beamTime = beamTime;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
