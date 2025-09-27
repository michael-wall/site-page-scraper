package com.mw.site.crawler.config;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.Type;

@ExtendedObjectClassDefinition(category = "site-page-crawler", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = SitePageCrawlerConfiguration.PID, localization = "content/Language", name = "configuration.site-page-crawler.name", description="configuration.site-page-crawler.desc")
public interface SitePageCrawlerConfiguration {
	public static final String PID = "com.mw.site.crawler.config.SitePageCrawlerConfiguration";
	
	public interface OUTPUT_TYPE {
		public static final String EXCEL = ".xlsx";
		
		public static final String TEXT = ".txt";
	}
	
	@Meta.AD(deflt = OUTPUT_TYPE.EXCEL, 
		required = false, 
		type = Type.String,
		name = "field.outputType.name", 
		description = "field.outputType.desc",
		optionValues = {OUTPUT_TYPE.EXCEL, OUTPUT_TYPE.TEXT},
		optionLabels = {"field.outputType.excel.label", "field.outputType.text.label"})
	public String outputType();	
	
	@Meta.AD(deflt = "true", required = false, type = Type.Boolean, name = "field.webContentDisplayWidgetLinksOnly.name", description = "field.webContentDisplayWidgetLinksOnly.desc")
	public boolean webContentDisplayWidgetLinksOnly();

	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.runAsGuestUser.name", description = "field.runAsGuestUser.desc")
	public boolean runAsGuestUser();	
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.useCurrentUsersLocaleWhenRunAsGuestUser.name", description = "field.useCurrentUsersLocaleWhenRunAsGuestUser.desc")
	public boolean useCurrentUsersLocaleWhenRunAsGuestUser();
	
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
	
	@Meta.AD(deflt = "false", required = false, type = Type.Boolean, name = "field.skipExternalLinks.name", description = "field.skipExternalLinks.desc")
	public boolean skipExternalLinks();
}