package com.mw.crawler.web.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ParamUtil;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import java.io.IOException;
import java.util.Map;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Michael Wall
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.tools",
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
	
	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("Activating...");
	}	
	
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {

		boolean sitePageCrawlerTriggered = ParamUtil.getBoolean(renderRequest, "sitePageCrawlerTriggered", false);
		String sitePageCrawlerStartTime = ParamUtil.getString(renderRequest, "sitePageCrawlerStartTime", null);
		
		renderRequest.setAttribute("sitePageCrawlerTriggered", sitePageCrawlerTriggered);
		renderRequest.setAttribute("sitePageCrawlerStartTime", sitePageCrawlerStartTime);

		super.doView(renderRequest, renderResponse);
		
		return;
	}	
	
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerPortlet.class);		
}