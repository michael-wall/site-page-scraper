package com.mw.site.crawler.model;

public class VirtualHostTO {

	private String hostName;
	private boolean publicVirtualHost;
	private String languageId;
	private boolean defaultLanguage;
	
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
	
	public boolean isDefaultLanguage() {
		return defaultLanguage;
	}
	public void setDefaultLanguage(boolean defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
	public VirtualHostTO(String hostName, boolean publicVirtualHost, String languageId, boolean defaultLanguage) {
		super();
		this.hostName = hostName;
		this.publicVirtualHost = publicVirtualHost;
		this.languageId = languageId;
		this.defaultLanguage = defaultLanguage;
	}
}