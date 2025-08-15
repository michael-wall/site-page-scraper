package com.mw.site.crawler.config;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.Type;

@ExtendedObjectClassDefinition(category = "site-page-crawler", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = SitePageCrawlerInfraConfiguration.PID, localization = "content/Language", name = "configuration.site-page-crawler-infra.name", description="configuration.site-page-crawler-infra.desc")
public interface SitePageCrawlerInfraConfiguration {
	public static final String PID = "com.mw.site.crawler.config.SitePageCrawlerInfraConfiguration";
	public static final int DEFAULT_TIMEOUT = 10000;
	
	public static final String DEFAULT_CRAWLER_USER_AGENT = "Liferay Site Page Crawler";
	
	@Meta.AD(deflt = "/mnt/persistent-storage/", required = false, type = Type.String, name = "field.outputFolder.name", description = "field.outputFolder.desc")
	public String outputFolder();
	
	@Meta.AD(deflt = "CRAWLER_OUTPUT", required = false, type = Type.String, name = "field.objectDefinitionERC.name", description = "field.objectDefinitionERC.desc")
	public String objectDefinitionERC();
	
	@Meta.AD(deflt = "section#content", required = false, type = Type.String, name = "field.pageBodySelector.name", description = "field.pageBodySelector.desc")
	public String pageBodySelector();
	
	@Meta.AD(deflt = DEFAULT_CRAWLER_USER_AGENT, required = false, type = Type.String, name = "field.crawlerUserAgent.name", description = "field.crawlerUserAgent.desc")
	public String crawlerUserAgent();
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.connectTimeout.name", description = "field.connectTimeout.desc")
	public int connectTimeout();
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.connectionRequestTimeout.name", description = "field.connectionRequestTimeout.desc")
	public int connectionRequestTimeout();	
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.socketTimeout.name", description = "field.socketTimeout.desc")
	public int socketTimeout();
}