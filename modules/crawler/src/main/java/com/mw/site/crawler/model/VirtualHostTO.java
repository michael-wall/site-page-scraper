package com.mw.site.crawler.model;

public class VirtualHostTO {

	private String hostName;
	private boolean publicVirtualHost;
	private String languageId;
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getLanguageId() {
		return languageId;
	}
	public void setLanguageId(String languageId) {
		this.languageId = languageId;
	}
	public boolean isPublicVirtualHost() {
		return publicVirtualHost;
	}
	public void setPublicVirtualHost(boolean publicVirtualHost) {
		this.publicVirtualHost = publicVirtualHost;
	}
	
	public VirtualHostTO(String hostName, boolean publicVirtualHost, String languageId) {
		super();
		this.hostName = hostName;
		this.publicVirtualHost = publicVirtualHost;
		this.languageId = languageId;
	}
}