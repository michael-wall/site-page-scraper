package com.mw.crawler.web.notification;

import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.notifications.UserNotificationDeliveryType;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import org.osgi.service.component.annotations.Component;

@Component(
		property = "javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET,
		service = UserNotificationDefinition.class
	)
public class SitePageCrawlerUserNotificationDefinition extends UserNotificationDefinition {
	
	public SitePageCrawlerUserNotificationDefinition() {
		super(
			CrawlerPortletKeys.CRAWLER_PORTLET, 0,
			UserNotificationDefinition.NOTIFICATION_TYPE_ADD_ENTRY,
			"receive-a-notification-when-the-asynchronous-site-page-crawler-completes");

		addUserNotificationDeliveryType(
			new UserNotificationDeliveryType(
				"website", UserNotificationDeliveryConstants.TYPE_WEBSITE, true,
				true));
	}
}