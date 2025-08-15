package com.mw.site.crawler.config;

import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

public class InfraConfigTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String crawlerUserAgent = SitePageCrawlerInfraConfiguration.DEFAULT_CRAWLER_USER_AGENT;
	private int connectTimeout = SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;
	private int connectionRequestTimeout = SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;
	private int socketTimeout = SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;

	public InfraConfigTO(String crawlerUserAgent, int connectTimeout, int connectionRequestTimeout, int socketTimeout) {
		super();
		
		this.crawlerUserAgent = crawlerUserAgent;
		this.connectTimeout = connectTimeout;
		this.connectionRequestTimeout = connectionRequestTimeout;
		this.socketTimeout = socketTimeout;
	}
	
	public String getCrawlerUserAgent() {
		if (Validator.isNull(crawlerUserAgent)) return SitePageCrawlerInfraConfiguration.DEFAULT_CRAWLER_USER_AGENT;
		
		return crawlerUserAgent;
	}

	public int getConnectTimeout() {
		if (connectTimeout <=0) return SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;
		
		return connectTimeout;
	}

	public int getConnectionRequestTimeout() {
		if (connectionRequestTimeout <=0) return SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;
		
		return connectionRequestTimeout;
	}
	
	public int getSocketTimeout() {
		if (socketTimeout <=0) return SitePageCrawlerInfraConfiguration.DEFAULT_TIMEOUT;
		
		return socketTimeout;
	}
}