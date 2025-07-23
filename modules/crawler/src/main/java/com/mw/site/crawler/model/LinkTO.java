package com.mw.site.crawler.model;

import java.io.Serializable;

public class LinkTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
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