package com.tapped.nfc;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class TagReadEntity implements MappedEntity {

	private String id;
	private String tagMessage;
	private Long tagReadTime;
	
	public TagReadEntity(){}
	
	public TagReadEntity(String id, String tagMessage, long tagReadTime){
		this.id = id;
		this.tagMessage = tagMessage;
		this.tagReadTime = tagReadTime;
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] { 
				new MappedField("id", "_id"), 
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
}
