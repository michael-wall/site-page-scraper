package com.mw.crawler.web.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.mw.crawler.web.constants.CrawlerPortletKeys;
import com.mw.site.crawler.SitePageLinkCrawler;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.model.VirtualHostTO;
import com.mw.site.crawler.util.CrawlerUtil;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael Wall
 */
@Component(
	immediate = true, 
	property = {
		"javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET,
		"mvc.command.name=/home",
		"mvc.command.name=/"
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
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

		if (Validator.isNull(themeDisplay.getUser()) || themeDisplay.getUser().isGuestUser()) {
			return "/noAccess.jsp";
		}
        
        long publicPageCount = layoutLocalService.getLayoutsCount(themeDisplay.getSiteGroupId(), false);
        long privatePageCount = layoutLocalService.getLayoutsCount(themeDisplay.getSiteGroupId(), true);
		String currentHostname = CrawlerUtil.getRelativeUrlPrefix(themeDisplay);
		String currentUserLocaleLabel = themeDisplay.getUser().getLocale().toString();
		String defaultLocaleLabel = CrawlerUtil.getSiteDefaultLocale(themeDisplay.getSiteGroupId()).toString();
	
		List<VirtualHostTO> virtualHosts = CrawlerUtil.getSiteVirtualHosts(themeDisplay.getSiteGroupId(), defaultLocaleLabel);
		
		int publicVirtualHostCount = 0;
		int privateVirtualHostCount = 0;
		
		for (VirtualHostTO vh: virtualHosts) {
			if (vh.isPublicVirtualHost()) publicVirtualHostCount ++;
			if (!vh.isPublicVirtualHost()) privateVirtualHostCount ++;
		}
		
		
		if (publicVirtualHostCount > 0) {
			renderRequest.setAttribute("hasPublicVirtualHosts", true);	
		} else {
			renderRequest.setAttribute("hasPublicVirtualHosts", false);
		}
		
		if (privateVirtualHostCount > 0) {
			renderRequest.setAttribute("hasPrivateVirtualHosts", true);	
		} else {
			renderRequest.setAttribute("hasPrivateVirtualHosts", false);
		}		
		
		ConfigTO config = sitePageLinkCrawler.getDefaultConfiguration();

		renderRequest.setAttribute("publicPageCount", publicPageCount);
		renderRequest.setAttribute("privatePageCount", privatePageCount);
		renderRequest.setAttribute("currentHostname", currentHostname);
		renderRequest.setAttribute("currentUserLocaleLabel", currentUserLocaleLabel);
		renderRequest.setAttribute("defaultLocaleLabel", defaultLocaleLabel);
		renderRequest.setAttribute("sitePageCrawlerConfig", config);

		return "/crawler.jsp";
	}
	
	@Reference(unbind = "-")
	private SitePageLinkCrawler sitePageLinkCrawler;
	
	@Reference
	private LayoutLocalService layoutLocalService;
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerPortletHomeRenderCommand.class);	
}