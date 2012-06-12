package com.redhat.contentspec.client.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Snap {
	private Integer id = 0;
	private String title = null;
	private Integer specId = null;
	private Date date = null;
	private String specTitle = null;
	
	public Snap(Integer id, Integer specId, String title, Date date, String specTitle) {
		this.id = id;
		this.title = title;
		this.specId = specId;
		this.date = date;
		this.specTitle = specTitle;
	}
	
	public Snap() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
		return String.format("ID: %s, Spec ID: %s, Title: %s, Date: %s", Integer.toString(id), specId.toString(), title, dateFormatter.format(date));
	}

	public Integer getSpecId() {
		return specId;
	}

	public void setSpecId(Integer specId) {
		this.specId = specId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSpecTitle() {
		return specTitle;
	}

	public void setSpecTitle(String specTitle) {
		this.specTitle = specTitle;
	}
}
