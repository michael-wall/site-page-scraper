package com.mw.site.crawler.model;

import java.io.Serializable;

public class LinkTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final long EXCEPTION_STATUS_CODE = -1;
	public static final long SKIPPED_EXTERNAL_LINK_STATUS_CODE = -2;
	public static final long SKIPPED_PRIVATE_PAGE_STATUS_CODE = -3;
	public static final long LOGIN_REDIRECT_STATUS_CODE = -4;
	public static final long UNEXPECTED_EXTERNAL_REDIRECT_STATUS_CODE = -5;
	
	private String href;
	private String label;
	private String statusCode;
	private String statusMessage;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public LinkTO(String href, String label, String statusCode, String statusMessage) {
		super();
		this.href = href;
		this.label = label;
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}
}