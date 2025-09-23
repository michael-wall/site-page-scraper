package com.mw.site.crawler.model;

public class VirtualHostTO {

	private long companyId;
	private long siteGroupId;
	private boolean isSiteVirtualHost;
	private String hostName;
	private boolean publicVirtualHost;
	private String languageId;
	private boolean defaultLanguage;
	
	public long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	
	public long getSiteGroupId() {
		return siteGroupId;
	}
	public void setSiteGroupId(long siteGroupId) {
		this.siteGroupId = siteGroupId;
	}
	
	public boolean isSiteVirtualHost() {
		return isSiteVirtualHost;
	}
	public void setSiteVirtualHost(boolean isSiteVirtualHost) {
		this.isSiteVirtualHost = isSiteVirtualHost;
	}
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
	public VirtualHostTO(long companyId, long siteGroupId, boolean isSiteVirtualHost, String hostName, boolean publicVirtualHost, String languageId, boolean defaultLanguage) {
		super();
		
		this.companyId = companyId;
		this.siteGroupId = siteGroupId;
		this.isSiteVirtualHost = isSiteVirtualHost;
		this.hostName = hostName;
		this.publicVirtualHost = publicVirtualHost;
		this.languageId = languageId;
		this.defaultLanguage = defaultLanguage;
	}
}