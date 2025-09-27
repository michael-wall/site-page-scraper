package com.mw.site.crawler.output;

public class SimpleOutputTO {

	private String label;
	private String value;
	
	private long longValue;
	private boolean storingLong = false;
	
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
	
	public long getLongValue() {
		return longValue;
	}
	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
	
	public boolean isStoringLong() {
		return storingLong;
	}
	
	public SimpleOutputTO(String label, String value) {
		super();
		this.label = label;
		this.value = value;
		this.storingLong = false;
	}
	
	public SimpleOutputTO(String label, long longValue) {
		super();
		this.label = label;
		this.longValue = longValue;
		this.storingLong = true;
	}
}