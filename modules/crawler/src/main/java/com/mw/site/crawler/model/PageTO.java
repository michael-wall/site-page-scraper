package com.mw.site.crawler.model;

import java.io.Serializable;
import java.util.List;

public class PageTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String url;
	
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

	public List<LinkTO> getLinks() {
		return links;
	}

	public void setLinks(List<LinkTO> links) {
		this.links = links;
	}
	
	public PageTO() {
		super();
	}

	public PageTO(String name, String url, List<LinkTO> links) {
		super();
		this.name = name;
		this.url = url;
		this.links = links;
	}
}
