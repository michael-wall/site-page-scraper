package com.mw.site.crawler.model;

import java.io.Serializable;

public class LinkTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String href;
	private String label;
	
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
	
	public LinkTO(String href, String label) {
		super();
		this.href = href;
		this.label = label;
	}

}
