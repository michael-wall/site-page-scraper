package com.mw.site.crawler.config;

import java.io.Serializable;

public class ConfigTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean webContentDisplayWidgetLinksOnly = true;
	private boolean includePublicPages = true;
	private boolean includePrivatePages = true;
	private boolean includeHiddenPages = false;
	private boolean checkPageGuestRoleViewPermission = false;
	private boolean validateLinksOnPages = false;
	

	public ConfigTO(boolean webContentDisplayWidgetLinksOnly, boolean includePublicPages, boolean includePrivatePages,
			boolean includeHiddenPages, boolean checkPageGuestRoleViewPermission, boolean validateLinksOnPages) {
		super();
		
		this.webContentDisplayWidgetLinksOnly = webContentDisplayWidgetLinksOnly;
		this.includePublicPages = includePublicPages;
		this.includePrivatePages = includePrivatePages;
		this.includeHiddenPages = includeHiddenPages;
		this.checkPageGuestRoleViewPermission = checkPageGuestRoleViewPermission;
		this.validateLinksOnPages = validateLinksOnPages;
	}

	public boolean isWebContentDisplayWidgetLinksOnly() {
		return webContentDisplayWidgetLinksOnly;
	}

	public void setWebContentDisplayWidgetLinksOnly(boolean webContentDisplayWidgetLinksOnly) {
		this.webContentDisplayWidgetLinksOnly = webContentDisplayWidgetLinksOnly;
	}

	public boolean isIncludePublicPages() {
		return includePublicPages;
	}

	public void setIncludePublicPages(boolean includePublicPages) {
		this.includePublicPages = includePublicPages;
	}

	public boolean isIncludePrivatePages() {
		return includePrivatePages;
	}

	public void setIncludePrivatePages(boolean includePrivatePages) {
		this.includePrivatePages = includePrivatePages;
	}

	public boolean isIncludeHiddenPages() {
		return includeHiddenPages;
	}

	public void setCrawlHiddenPages(boolean includeHiddenPages) {
		this.includeHiddenPages = includeHiddenPages;
	}

	public boolean isCheckPageGuestRoleViewPermission() {
		return checkPageGuestRoleViewPermission;
	}

	public void setCheckPageGuestRoleViewPermission(boolean checkPageGuestRoleViewPermission) {
		this.checkPageGuestRoleViewPermission = checkPageGuestRoleViewPermission;
	}	

	public boolean isValidateLinksOnPages() {
		return validateLinksOnPages;
	}

	public void setValidateLinksOnPages(boolean validateLinksOnPages) {
		this.validateLinksOnPages = validateLinksOnPages;
	}
}