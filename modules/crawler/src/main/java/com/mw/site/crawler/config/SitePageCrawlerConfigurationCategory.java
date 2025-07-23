package com.mw.site.crawler.config;

import com.liferay.configuration.admin.category.ConfigurationCategory;

import org.osgi.service.component.annotations.Component;

@Component
public class SitePageCrawlerConfigurationCategory implements ConfigurationCategory {

	@Override
	public String getCategoryIcon() {
		return "page";
	}

	@Override
	public String getCategoryKey() {
		return "site-page-crawler";
	}

	@Override
	public String getCategorySection() {
		return "content-and-data";
	}
}