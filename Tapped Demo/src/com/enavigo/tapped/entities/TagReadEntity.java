package com.enavigo.tapped.entities;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class TagReadEntity implements MappedEntity {

	private String id;
	private String tagMessage;
	private String userName;
	private Long tagReadTime;
	
	public TagReadEntity(){}
	
	public TagReadEntity(String userName, String tagMessage, long tagReadTime){
		this.userName = userName;
		this.tagMessage = tagMessage;
		this.tagReadTime = tagReadTime;
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] { 
				new MappedField("id", "_id"), 
				new MappedField("userName", "userName"),
				new MappedField("tagMessage", "tagMessage"),
				new MappedField("tagReadTime", "tagReadTime")});
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTagMessage() {
		return tagMessage;
	}

	public void setTagMessage(String tagMessage) {
		this.tagMessage = tagMessage;
	}

	public Long getTagReadTime() {
		return tagReadTime;
	}

	public void setTagReadTime(Long tagReadTime) {
		this.tagReadTime = tagReadTime;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
