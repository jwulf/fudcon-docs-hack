package com.redhat.contentspec.client.config;

public class ContentSpecConfiguration {

	private String serverUrl;
	private Integer contentSpecId;
	private String rootOutputDir;
	
	public Integer getContentSpecId() {
		return contentSpecId;
	}
	
	public void setContentSpecId(Integer contentSpecId) {
		this.contentSpecId = contentSpecId;
	}
	
	public String getServerUrl() {
		return serverUrl;
	}
	
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getRootOutputDirectory() {
		return rootOutputDir;
	}

	public void setRootOutputDirectory(String rootOutputDir) {
		this.rootOutputDir = rootOutputDir;
	}
}
