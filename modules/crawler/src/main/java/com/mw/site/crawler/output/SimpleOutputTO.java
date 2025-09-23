package com.mw.site.crawler.output;

public class SimpleOutputTO {

	String label;
	String value;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public SimpleOutputTO(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
}