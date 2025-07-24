package com.mw.crawler.web.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import java.util.Map;

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
		"javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET,
		"mvc.command.name=/home"
	},
	service = MVCRenderCommand.class
)
public class CrawlerPortletHomeRenderCommand implements MVCRenderCommand {
	
	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("Activating...");
	}	
	
	@Override
	public String render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException {
		boolean sitePageCrawlerTriggered = ParamUtil.getBoolean(renderRequest, "sitePageCrawlerTriggered", false);
		String sitePageCrawlerStartTime = ParamUtil.getString(renderRequest, "sitePageCrawlerStartTime", null);
		boolean sitePageCrawlerNoPagesFound = ParamUtil.getBoolean(renderRequest, "sitePageCrawlerNoPagesFound", false);
		
		renderRequest.setAttribute("sitePageCrawlerTriggered", sitePageCrawlerTriggered);
		renderRequest.setAttribute("sitePageCrawlerStartTime", sitePageCrawlerStartTime);
		renderRequest.setAttribute("sitePageCrawlerNoPagesFound", sitePageCrawlerNoPagesFound);
		
		return "/crawler.jsp";
	}
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerPortletHomeRenderCommand.class);	
}