package com.mw.crawler.web.portlet;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.mw.crawler.web.constants.CrawlerPortletKeys;
import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.SitePageLinkCrawler;
import com.mw.site.crawler.config.SitePageCrawlerConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletRequest;

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
		"mvc.command.name=/crawlPages" },
	configurationPid = SitePageCrawlerConfiguration.PID,
	service = MVCActionCommand.class
)
public class CrawlerPortletActionCommand extends BaseMVCActionCommand {
	
	@Activate
	@Modified
    protected void activate(Map<String, Object> properties) throws Exception {		
		if (_log.isInfoEnabled()) _log.info("Activating...");		
		
		_sitePageCrawlerConfiguration = ConfigurableUtil.createConfigurable(SitePageCrawlerConfiguration.class, properties);
		
		_log.info("outputFolder: " + _sitePageCrawlerConfiguration.outputFolder());
		
		_log.info("validateLinksOnPages: " + _sitePageCrawlerConfiguration.validateLinksOnPages());
	}		
	
	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		
		_log.info("doView");
		
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
		HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(actionRequest);
		
		long companyId = themeDisplay.getCompanyId();
		long siteId = themeDisplay.getSiteGroupId();
		boolean validateLinksOnPages = _sitePageCrawlerConfiguration.validateLinksOnPages();

		String relativeUrlPrefix = getRelativeUrlPrefix(themeDisplay);
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		String siteFriendlyUrl = group.getFriendlyURL();
		
		String publicPrefix = PortalUtil.getPathFriendlyURLPublic();
        String privatePrefix = PortalUtil.getPathFriendlyURLPrivateGroup();
		
		String publicLayoutUrlPrefix = relativeUrlPrefix + publicPrefix + siteFriendlyUrl;
		String privateLayoutUrlPrefix = relativeUrlPrefix + privatePrefix + siteFriendlyUrl;
		
		String cookieDomain = themeDisplay.getServerName();
		String outputFolder = _sitePageCrawlerConfiguration.outputFolder();
		
		LayoutCrawler layoutCrawler = new LayoutCrawler(publicLayoutUrlPrefix, privateLayoutUrlPrefix, httpRequest, cookieDomain, themeDisplay.getUser());
		
		// Asynchronous...
		TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
		    @Override
		    public Void call() throws Exception {
		        // Use ExecutorService instead of raw threads
		        Executors.newSingleThreadExecutor().submit(() -> {
		            try {
		            	_log.info("Background Crawler Started ...");
		            	
		            	String response = sitePageLinkCrawler.crawlPage(companyId, group, validateLinksOnPages, relativeUrlPrefix, themeDisplay.getUser(), outputFolder, layoutCrawler);
		            	
		            	if (Validator.isNotNull(response)) {
		            		_log.info("Output written to: " + response);
		            		
		            		// TODO Do something here... for example send an email with the file attached...
		            	}
		            	
		                _log.info("Background Crawler Completed ...");
		            } catch (Exception e) {
		                _log.error("Error in Background Crawler Thread", e);
		            }
		        });

		        return null;
		    }
		});
		
		ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String startTimeString = nowUtc.format(formatter) + " UTC";
		
		actionResponse.setRenderParameter("sitePageCrawlerTriggered", "true");
		actionResponse.setRenderParameter("sitePageCrawlerStartTime", startTimeString);
		actionResponse.setRenderParameter("mvcRenderCommandName", "/home");
		
		return;
	}
	
	private String getRelativeUrlPrefix(ThemeDisplay themeDisplay) {
		
		String protocol = themeDisplay.getPortalURL().split(":")[0]; // http or https
        String host = themeDisplay.getServerName();
        int port = themeDisplay.getServerPort();
		
		String prefix = protocol + "://" + host;
		if (port != 80 && port != 443) prefix += ":" + port;
		
		return prefix;
	}
	
	@Reference(unbind = "-")
	private SitePageLinkCrawler sitePageLinkCrawler;
	
	@Reference(unbind = "-")
	private GroupLocalService groupLocalService;
	
	private volatile SitePageCrawlerConfiguration _sitePageCrawlerConfiguration;	
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerPortletActionCommand.class);	
}