package com.mw.site.crawler.config;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.Type;

@ExtendedObjectClassDefinition(category = "site-page-crawler", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = SitePageCrawlerConfiguration.PID, localization = "content/Language", name = "configuration.site-page-crawler.name", description="configuration.site-page-crawler.desc")
public interface SitePageCrawlerConfiguration {
	public static final String PID = "com.mw.site.crawler.config.SitePageCrawlerConfiguration";
	public static final int DEFAULT_TIMEOUT = 10000;
	
	public static final String DEFAULT_CRAWLER_USER_AGENT = "Liferay Site Page Crawler";
	
	@Meta.AD(deflt = "/mnt/persistent-storage/", required = false, type = Type.String, name = "field.outputFolder.name", description = "field.outputFolder.desc")
	public String outputFolder();
	
	@Meta.AD(deflt = "CRAWLER_OUTPUT", required = false, type = Type.String, name = "field.objectDefinitionERC.name", description = "field.objectDefinitionERC.desc")
	public String objectDefinitionERC();
	
	@Meta.AD(deflt = "section#content", required = false, type = Type.String, name = "field.pageBodySelector.name", description = "field.pageBodySelector.desc")
	public String pageBodySelector();
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.webContentDisplayWidgetLinksOnly.name", description = "field.webContentDisplayWidgetLinksOnly.desc")
	public boolean webContentDisplayWidgetLinksOnly();

	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.runAsGuestUser.name", description = "field.runAsGuestUser.desc")
	public boolean runAsGuestUser();	
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.includePublicPages.name", description = "field.includePublicPages.desc")
	public boolean includePublicPages();	
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.includePrivatePages.name", description = "field.includePrivatePages.desc")
	public boolean includePrivatePages();	
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.includeHiddenPages.name", description = "field.includeHiddenPages.desc")
	public boolean includeHiddenPages();
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.checkPageGuestRoleViewPermission.name", description = "field.checkPageGuestRoleViewPermission.desc")
	public boolean checkPageGuestRoleViewPermission();
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.validateLinksOnPages.name", description = "field.validateLinksOnPages.desc")
	public boolean validateLinksOnPages();
	
	@Meta.AD(deflt = DEFAULT_CRAWLER_USER_AGENT, required = false, type = Type.String, name = "field.crawlerUserAgent.name", description = "field.crawlerUserAgent.desc")
	public String crawlerUserAgent();
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.connectTimeout.name", description = "field.connectTimeout.desc")
	public int connectTimeout();
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.connectionRequestTimeout.name", description = "field.connectionRequestTimeout.desc")
	public int connectionRequestTimeout();	
	
	@Meta.AD(deflt = "" + DEFAULT_TIMEOUT, required = false, type = Type.String, name = "field.socketTimeout.name", description = "field.socketTimeout.desc")
	public int socketTimeout();	
	

}