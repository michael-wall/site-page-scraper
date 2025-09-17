package com.mw.crawler.web.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

/**
 * @author Michael Wall
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/crawler.jsp",
		"javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET,
		"javax.portlet.resource-bundle=content.Language",
		"com.liferay.portlet.show-portlet-access-denied=false",
		"javax.portlet.version=3.0"
	},
	service = Portlet.class
)
public class CrawlerPortlet extends MVCPortlet {
	
}