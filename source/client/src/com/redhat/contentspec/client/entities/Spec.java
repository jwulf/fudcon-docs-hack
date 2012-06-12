package com.redhat.contentspec.client.entities;

public class Spec {
	
	private Integer id = 0;
	private String title = null;
	private String product = null;
	private String version = null;
	private String creator = null;
	
	public Spec(Integer id, String title, String product, String version, String creator) {
		this.id = id;
		this.title = title;
		this.product = product;
		this.version = version;
		this.creator = creator;
	}
	
	public Spec() {
		
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

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String toString() {
		return String.format("ID: %s, Title: %s, Product: %s, Version: %s, Created By: %s", Integer.toString(id), title, product, version, creator);
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
}
