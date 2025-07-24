package com.mw.site.crawler.model;

import java.io.Serializable;

public class ResponseTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private boolean success;
	private String filePath;
	private String statusMessage;
	private long crawledPages;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	public long getCrawledPages() {
		return crawledPages;
	}
	public void setCrawledPages(long crawledPages) {
		this.crawledPages = crawledPages;
	}
	public ResponseTO(boolean success, String filePath, String statusMessage, long crawledPages) {
		super();
		this.success = success;
		this.filePath = filePath;
		this.statusMessage = statusMessage;
		this.crawledPages = crawledPages;
	}
}