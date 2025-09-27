package com.mw.site.crawler.model;

import java.io.Serializable;
import java.util.List;

public class PageTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String friendlyUrl;
	private String url;
	private boolean privatePage = false;
	private boolean hiddenPage = false;
	private long validLinkCount = 0;
	private long invalidLinkCount = 0;
	private long skippedExternalLinkCount = 0;
	private long skippedPrivateLinkCount = 0;
	private long loginRequiredLinkCount = 0;
	private long unexpectedExternalRedirectLinkCount = 0;
	private int guestRoleViewPermissionEnabled = -1;
	
	private List<LinkTO> links;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFriendlyUrl() {
		return friendlyUrl;
	}

	public void setFriendlyUrl(String friendlyUrl) {
		this.friendlyUrl = friendlyUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isPrivatePage() {
		return privatePage;
	}

	public void setPrivatePage(boolean privatePage) {
		this.privatePage = privatePage;
	}

	public boolean isHiddenPage() {
		return hiddenPage;
	}

	public void setHiddenPage(boolean hiddenPage) {
		this.hiddenPage = hiddenPage;
	}

	public long getValidLinkCount() {
		return validLinkCount;
	}

	public void setValidLinkCount(long validLinkCount) {
		this.validLinkCount = validLinkCount;
	}
	
	public long getLoginRequiredLinkCount() {
		return loginRequiredLinkCount;
	}

	public void setLoginRequiredLinkCount(long loginRequiredLinkCount) {
		this.loginRequiredLinkCount = loginRequiredLinkCount;
	}
	
	public long getUnexpectedExternalRedirectLinkCount() {
		return unexpectedExternalRedirectLinkCount;
	}

	public void setUnexpectedExternalRedirectLinkCount(long unexpectedExternalRedirectLinkCount) {
		this.unexpectedExternalRedirectLinkCount = unexpectedExternalRedirectLinkCount;
	}

	public long getInvalidLinkCount() {
		return invalidLinkCount;
	}

	public void setInvalidLinkCount(long invalidLinkCount) {
		this.invalidLinkCount = invalidLinkCount;
	}

	public long getSkippedExternalLinkCount() {
		return skippedExternalLinkCount;
	}

	public void setSkippedExternalLinkCount(long skippedExternalLinkCount) {
		this.skippedExternalLinkCount = skippedExternalLinkCount;
	}

	public long getSkippedPrivateLinkCount() {
		return skippedPrivateLinkCount;
	}

	public void setSkippedPrivateLinkCount(long skippedPrivateLinkCount) {
		this.skippedPrivateLinkCount = skippedPrivateLinkCount;
	}

	public List<LinkTO> getLinks() {
		return links;
	}

	public void setLinks(List<LinkTO> links) {
		this.links = links;
	}

	public int getGuestRoleViewPermissionEnabled() {
		return guestRoleViewPermissionEnabled;
	}

	public void setGuestRoleViewPermissionEnabled(int guestRoleViewPermissionEnabled) {
		this.guestRoleViewPermissionEnabled = guestRoleViewPermissionEnabled;
	}

	public PageTO() {
		super();
	}
}