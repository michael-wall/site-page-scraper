package com.mw.crawler.web.portlet;

import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.mw.crawler.web.constants.CrawlerPortletKeys;
import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.SitePageLinkCrawler;
import com.mw.site.crawler.config.SitePageCrawlerConfiguration;
import com.mw.site.crawler.model.ResponseTO;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
		
		_log.info("objectDefinitionERC: " + _sitePageCrawlerConfiguration.objectDefinitionERC());
	}		
	
	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
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
		
		List<Layout> layouts = sitePageLinkCrawler.getPages(siteId, true);
		
		if (layouts == null || layouts.isEmpty()) {
			actionResponse.setRenderParameter("sitePageCrawlerNoPagesFound", "true");
			actionResponse.setRenderParameter("mvcRenderCommandName", "/home");
			
			return;
		}
		
		// Asynchronous...
		TransactionCommitCallbackUtil.registerCallback(new Callable<Void>() {
		    @Override
		    public Void call() throws Exception {
		        Executors.newSingleThreadExecutor().submit(() -> {
		            try {
		            	_log.info("Background Crawler Started for Site " + group.getName(themeDisplay.getUser().getLocale()) + " for " + themeDisplay.getUser().getFullName());
		            	
		            	ResponseTO responseTO = sitePageLinkCrawler.crawlPage(companyId, group, validateLinksOnPages, relativeUrlPrefix, themeDisplay.getUser(), outputFolder, layoutCrawler, layouts);
		            		            		
		            	ObjectEntry objectEntry = null;
		            	String objectDefinitionLabel = null;
		            	
		            	if (responseTO.isSuccess() && Validator.isNotNull(responseTO.getFilePath())) {
		            		ObjectDefinition crawlerOutputDefinition = null;
		            		
		            		if (Validator.isNotNull(_sitePageCrawlerConfiguration.objectDefinitionERC())) {
		            			crawlerOutputDefinition = objectDefinitionLocalService.fetchObjectDefinitionByExternalReferenceCode(_sitePageCrawlerConfiguration.objectDefinitionERC(), companyId);
		            		}
		            		
		            		if (crawlerOutputDefinition != null) {
		            			objectDefinitionLabel = crawlerOutputDefinition.getPluralLabel(themeDisplay.getUser().getLocale());
		            			
		            	        Path path = Paths.get(responseTO.getFilePath());
		            	        String fileName = path.getFileName().toString();
		            	        String fileMimeType = Files.probeContentType(path);
		            	        byte[] fileBytes = Files.readAllBytes(path);
		            			
		            	        FileEntry fileEntry = dlAppLocalService.addFileEntry(UUID.randomUUID().toString(), themeDisplay.getUser().getUserId(), siteId, 0, fileName, fileMimeType, fileBytes, null, null, new ServiceContext());        			

		            			Map<String, Serializable> objectEntryFields = new HashMap<>();
		            			objectEntryFields.put("requestor", themeDisplay.getUser().getFullName());
		            			objectEntryFields.put("site", group.getName(themeDisplay.getUser().getLocale()));
		            			objectEntryFields.put("output", fileEntry.getFileEntryId());
		            			objectEntryFields.put("validateLinksOnPages", validateLinksOnPages); 
		            			objectEntryFields.put("crawledPages", responseTO.getCrawledPages()); 
		            	        
		            	        objectEntry = objectEntryLocalService.addObjectEntry(
		            	        	themeDisplay.getUser().getUserId(),
		            	        	0,
		            	        	crawlerOutputDefinition.getObjectDefinitionId(),
		            	        	objectEntryFields,
		            	        	new ServiceContext()
		            	        );
		            	        
		            	        _log.info(objectDefinitionLabel + " Object Record " + objectEntry.getObjectEntryId() + " created for Site " + group.getName(themeDisplay.getUser().getLocale()) + " for " + themeDisplay.getUser().getFullName());
		            		} else {
		            			_log.info("Unable to create Object Record as Object Definition not found: " + _sitePageCrawlerConfiguration.objectDefinitionERC());
		            		}
		            	}
		            	
		            	JSONObject notificationJSON = JSONFactoryUtil.createJSONObject();

		            	notificationJSON.put("success", responseTO.isSuccess());
		            	notificationJSON.put("siteName", group.getName(themeDisplay.getUser().getLocale()));
		            	
		            	if (responseTO.isSuccess()) {
			            	notificationJSON.put("filePath", responseTO.getFilePath());

		            		if (objectEntry != null) {		            			
		            			notificationJSON.put("objectEntryId", objectEntry.getObjectEntryId());
		            			notificationJSON.put("objectDefinitionLabel", objectDefinitionLabel);
		            		}
		            	}
		   
		            	notificationJSON.put("statusMessage", responseTO.getStatusMessage());

		            	try {
		            		userNotificationEventLocalService.addUserNotificationEvent(
		            			themeDisplay.getUser().getUserId(),
		            			CrawlerPortletKeys.CRAWLER_PORTLET,
		            			System.currentTimeMillis(),
		            			UserNotificationDeliveryConstants.TYPE_WEBSITE,
		            			0,
		            			true,
		            			notificationJSON.toString(),
		            			false,
		            			false,
		            			new ServiceContext()
		            		);
		            			
		            		_log.info("Liferay Notification sent to " + themeDisplay.getUser().getFullName());
		            	} catch (Exception e) {
		            		_log.error("Error adding notification", e);
		            	}
		            	
		            	_log.info("Background Crawler Completed for Site " + group.getName(themeDisplay.getUser().getLocale()) + " for " + themeDisplay.getUser().getFullName());
		            } catch (Exception e) {
		                _log.error("Error in Background Crawler Thread for Site " + group.getName(themeDisplay.getUser().getLocale()) + " for " + themeDisplay.getUser().getFullName(), e);
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
	
	@Reference(unbind = "-")
	private UserNotificationEventLocalService userNotificationEventLocalService;
	
	private volatile SitePageCrawlerConfiguration _sitePageCrawlerConfiguration;
	
    @Reference
    private ObjectEntryLocalService objectEntryLocalService;

    @Reference
    private ObjectDefinitionLocalService objectDefinitionLocalService;
    
    @Reference
    private DLAppLocalService dlAppLocalService;    
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerPortletActionCommand.class);	
}