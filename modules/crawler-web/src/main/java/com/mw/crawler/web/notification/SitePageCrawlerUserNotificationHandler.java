package com.mw.crawler.web.notification;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler;
import com.liferay.portal.kernel.notifications.UserNotificationHandler;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Portal;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	    immediate = true,
	    property = "javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET,
	    service = UserNotificationHandler.class
	)
public class SitePageCrawlerUserNotificationHandler extends BaseUserNotificationHandler {

    public SitePageCrawlerUserNotificationHandler() {
        setPortletId(CrawlerPortletKeys.CRAWLER_PORTLET);
    }
    
	private String _getMessage(
			UserNotificationEvent userNotificationEvent,
			ServiceContext serviceContext)
		throws Exception {

		Locale locale = _portal.getLocale(serviceContext.getRequest());

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			userNotificationEvent.getPayload());

		String message = null;
		boolean success = jsonObject.getBoolean("success");
		String siteName = jsonObject.getString("siteName");
		long objectEntryId = 0;
		String filePath = null;
		String statusMessage = null;
		
		if (success) {
			filePath = jsonObject.getString("filePath");
			
			objectEntryId = jsonObject.getLong("objectEntryId", 0);
			
			if (objectEntryId == 0) {
				message = "asynchronous-site-page-crawler-successful";
				
				String[] arguments = { siteName, filePath };

				return _language.format(locale, message, arguments);			
			} else {
				message = "asynchronous-site-page-crawler-successful-uploaded";
				
				String objectDefinitionLabel = jsonObject.getString("objectDefinitionLabel");
				
				String[] arguments = { siteName, objectDefinitionLabel, "" + objectEntryId };

				return _language.format(locale, message, arguments);			
			}
		} else {
			statusMessage = jsonObject.getString("statusMessage");
			
			message = "asynchronous-site-page-crawler-unsuccessful";
			
			String[] arguments = { siteName, statusMessage };
			
			return _language.format(locale, message, arguments);
		}
	}
	
	@Override
	protected String getTitle(
			UserNotificationEvent userNotificationEvent,
			ServiceContext serviceContext)
		throws Exception {

		return _getMessage(userNotificationEvent, serviceContext);
	}	
    
	@Override
	protected String getBody(
			UserNotificationEvent userNotificationEvent,
			ServiceContext serviceContext)
		throws Exception {

		return _getMessage(userNotificationEvent, serviceContext);
	}    

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;	
}