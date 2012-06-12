package com.redhat.contentspec.client.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Revision {
	
	private Integer id = 0;
	private Date date = null;
	
	public Revision(Integer id, Date date) {
		this.id = id;
		this.date = date;
	}
	
	public Revision() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String toString() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy 'at' hh:mm:ss a");
		return String.format("-> ID: %d on %s", id, dateFormatter.format(date));
	}
}
