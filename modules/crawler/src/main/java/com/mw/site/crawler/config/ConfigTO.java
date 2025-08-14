package com.mw.site.crawler.config;

import java.io.Serializable;

public class ConfigTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean webContentDisplayWidgetLinksOnly = true;
	private boolean runAsGuestUser = false;
	private boolean includePublicPages = true;
	private boolean includePrivatePages = true;
	private boolean includeHiddenPages = false;
	private boolean checkPageGuestRoleViewPermission = false;
	private boolean validateLinksOnPages = false;

	private String crawlerUserAgent = SitePageCrawlerConfiguration.DEFAULT_CRAWLER_USER_AGENT;
	private int connectTimeout = SitePageCrawlerConfiguration.DEFAULT_TIMEOUT;
	private int connectionRequestTimeout = SitePageCrawlerConfiguration.DEFAULT_TIMEOUT;
	private int socketTimeout = SitePageCrawlerConfiguration.DEFAULT_TIMEOUT;

	public ConfigTO(boolean webContentDisplayWidgetLinksOnly, boolean runAsGuestUser, boolean includePublicPages, boolean includePrivatePages,
			boolean includeHiddenPages, boolean checkPageGuestRoleViewPermission, boolean validateLinksOnPages, String crawlerUserAgent, int connectTimeout, int connectionRequestTimeout, int socketTimeout) {
		super();
		
		this.webContentDisplayWidgetLinksOnly = webContentDisplayWidgetLinksOnly;
		this.runAsGuestUser = runAsGuestUser;
		this.includePublicPages = includePublicPages;
		this.includePrivatePages = includePrivatePages;
		this.includeHiddenPages = includeHiddenPages;
		this.checkPageGuestRoleViewPermission = checkPageGuestRoleViewPermission;
		this.validateLinksOnPages = validateLinksOnPages;
		
		this.crawlerUserAgent = crawlerUserAgent;
		this.connectTimeout = connectTimeout;
		this.connectionRequestTimeout = connectionRequestTimeout;
		this.socketTimeout = socketTimeout;
	}

	public boolean isWebContentDisplayWidgetLinksOnly() {
		return webContentDisplayWidgetLinksOnly;
	}

	public boolean isRunAsGuestUser() {
		return runAsGuestUser;
	}

	public boolean isIncludePublicPages() {
		return includePublicPages;
	}

	public boolean isIncludePrivatePages() {
		return includePrivatePages;
	}
	
	public boolean isIncludeHiddenPages() {
		return includeHiddenPages;
	}

	public boolean isCheckPageGuestRoleViewPermission() {
		return checkPageGuestRoleViewPermission;
	}

	public boolean isValidateLinksOnPages() {
		return validateLinksOnPages;
	}
	
	
	public String getCrawlerUserAgent() {
		return crawlerUserAgent;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}
	
	public int getSocketTimeout() {
		return socketTimeout;
	}
}