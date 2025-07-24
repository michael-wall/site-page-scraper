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
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.validateLinksOnPages.name", description = "field.validateLinksOnPages.desc")
	public boolean validateLinksOnPages();
	
	@Meta.AD(deflt = "CRAWLER_OUTPUT", required = false, type = Type.String, name = "field.objectDefinitionERC.name", description = "field.objectDefinitionERC.desc")
	public String objectDefinitionERC();
}