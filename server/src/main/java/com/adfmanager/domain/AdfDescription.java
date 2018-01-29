package com.adfmanager.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class AdfDescription {
	
	public static final String TYPE_ADF = "ADF";
	public static final String TYPE_SCENE = "Scene";
	
	@Id
	@SequenceGenerator(name = "SEQ_GEN", sequenceName = "SEQ_JUST_FOR_TEST", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN")
	private long id;
	
	private String type;
	
	private double lat;
	private double lng;
	private double alt;
	
	private int lvl;
	
	private String name;
	private String fileName;
	
	
	private String description;
	
	private String uuid;
	
	@JsonIgnore //TODO: Dont give out user id by json?
	private String userId;

	public AdfDescription() {
	}
	//TODO: Too many params, think about builder pattern
	public AdfDescription(double lat, double lng,int lvl,double alt, String name,String fileName, String description,String uuid,String userId,String type) {
		this.lat = lat;
		this.lng = lng;
		this.lvl = lvl;
		this.alt = alt;
		
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		
		this.uuid = uuid;
		
		this.userId = userId;
		
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("AdfDescription[id=%d, lat='.2f', lng='.2f', name=%s]", id, lng, lat, name);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getLvl() {
		return lvl;
	}

	public void setLvl(int lvl) {
		this.lvl = lvl;
	}
	
	public double getAlt() {
		return alt;
	}
	public void setAlt(double alt) {
		this.alt = alt;
	}

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
