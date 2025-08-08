package com.mw.site.crawler.config;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.Type;

@ExtendedObjectClassDefinition(category = "site-page-crawler", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = SitePageCrawlerConfiguration.PID, localization = "content/Language", name = "configuration.site-page-crawler.name", description="configuration.site-page-crawler.desc")
public interface SitePageCrawlerConfiguration {
	public static final String PID = "com.mw.site.crawler.config.SitePageCrawlerConfiguration";
	
	@Meta.AD(deflt = "/mnt/persistent-storage/", required = false, type = Type.String, name = "field.outputFolder.name", description = "field.outputFolder.desc")
	public String outputFolder();
	
	@Meta.AD(deflt = "CRAWLER_OUTPUT", required = false, type = Type.String, name = "field.objectDefinitionERC.name", description = "field.objectDefinitionERC.desc")
	public String objectDefinitionERC();
	
	@Meta.AD(deflt = "section#content", required = false, type = Type.String, name = "field.pageBodySelector.name", description = "field.pageBodySelector.desc")
	public String pageBodySelector();
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.webContentDisplayWidgetLinksOnly.name", description = "field.webContentDisplayWidgetLinksOnly.desc")
	public boolean webContentDisplayWidgetLinksOnly();

	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.crawlPublicPages.name", description = "field.crawlPublicPages.desc")
	public boolean crawlPublicPages();	
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.crawlPrivatePages.name", description = "field.crawlPrivatePages.desc")
	public boolean crawlPrivatePages();	
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.crawlHiddenPages.name", description = "field.crawlHiddenPages.desc")
	public boolean crawlHiddenPages();
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.checkPageGuestView.name", description = "field.checkPageGuestView.desc")
	public boolean checkPageGuestView();
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.validateLinksOnPages.name", description = "field.validateLinksOnPages.desc")
	public boolean validateLinksOnPages();
}