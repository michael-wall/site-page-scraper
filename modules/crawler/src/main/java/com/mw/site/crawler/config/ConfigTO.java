package com.mw.site.crawler.config;

import java.io.Serializable;

public class ConfigTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String outputType = SitePageCrawlerConfiguration.OUTPUT_TYPE.EXCEL;
	private boolean webContentDisplayWidgetLinksOnly = true;
	private boolean runAsGuestUser = false;
	private boolean useCurrentUsersLocaleWhenRunAsGuestUser = false;
	private boolean includePublicPages = true;
	private boolean includePrivatePages = true;
	private boolean includeHiddenPages = false;
	private boolean checkPageGuestRoleViewPermission = false;
	private boolean validateLinksOnPages = false;
	private boolean skipExternalLinks = false;
	
	public ConfigTO(String outputType, boolean webContentDisplayWidgetLinksOnly, boolean runAsGuestUser, boolean useCurrentUsersLocaleWhenRunAsGuestUser, boolean includePublicPages, boolean includePrivatePages,
			boolean includeHiddenPages, boolean checkPageGuestRoleViewPermission, boolean validateLinksOnPages, boolean skipExternalLinks) {
		super();
		
		this.outputType = outputType;
		this.webContentDisplayWidgetLinksOnly = webContentDisplayWidgetLinksOnly;
		this.runAsGuestUser = runAsGuestUser;
		this.useCurrentUsersLocaleWhenRunAsGuestUser = useCurrentUsersLocaleWhenRunAsGuestUser;
		this.includePublicPages = includePublicPages;
		this.includePrivatePages = includePrivatePages;
		this.includeHiddenPages = includeHiddenPages;
		this.checkPageGuestRoleViewPermission = checkPageGuestRoleViewPermission;
		this.validateLinksOnPages = validateLinksOnPages;
		this.skipExternalLinks = skipExternalLinks;
	}

	public String getOutputType() {
		return outputType;
	}
	
	public boolean isWebContentDisplayWidgetLinksOnly() {
		return webContentDisplayWidgetLinksOnly;
	}

	public boolean isRunAsGuestUser() {
		return runAsGuestUser;
	}
	
	public boolean isUseCurrentUsersLocaleWhenRunAsGuestUser() {
		return useCurrentUsersLocaleWhenRunAsGuestUser;
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

	public boolean isSkipExternalLinks() {
		return skipExternalLinks;
	}
	
	
	public String getExcel() {
		return SitePageCrawlerConfiguration.OUTPUT_TYPE.EXCEL;
	}
	
	public String getText() {
		return SitePageCrawlerConfiguration.OUTPUT_TYPE.TEXT;
	}
}