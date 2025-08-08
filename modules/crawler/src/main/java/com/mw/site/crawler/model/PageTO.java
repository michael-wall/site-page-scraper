package com.mw.site.crawler.model;

import java.io.Serializable;
import java.util.List;

public class PageTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String url;
	private boolean privatePage = false;
	private long validLinkCount = 0;
	private long invalidLinkCount = 0;
	private int guestRoleViewPermissionEnabled = -1;
	
	private List<LinkTO> links;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public long getValidLinkCount() {
		return validLinkCount;
	}

	public void setValidLinkCount(long validLinkCount) {
		this.validLinkCount = validLinkCount;
	}

	public long getInvalidLinkCount() {
		return invalidLinkCount;
	}

	public void setInvalidLinkCount(long invalidLinkCount) {
		this.invalidLinkCount = invalidLinkCount;
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