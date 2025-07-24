package com.mw.crawler.web.menu;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.application.list.constants.PanelCategoryKeys;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.permission.PortalPermissionUtil;
import com.mw.crawler.web.constants.CrawlerPortletKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael Wall
 */
@Component(
	property = {
		"panel.app.order:Integer=10000",
		"panel.category.key=" + PanelCategoryKeys.SITE_ADMINISTRATION_BUILD
	},
	service = PanelApp.class
)
public class CrawlerPanelApp extends BasePanelApp {
	
	@Override
	public Portlet getPortlet() {
		return _portlet;
	}

	@Override
	public String getPortletId() {
		return CrawlerPortletKeys.CRAWLER_PORTLET;
	}

	@Reference(
		target = "(javax.portlet.name=" + CrawlerPortletKeys.CRAWLER_PORTLET + ")"
	)
	private Portlet _portlet;	
	
	@Override
	public boolean isShow(PermissionChecker permissionChecker, Group group) throws PortalException {
		
		if (group.isUserGroup()) return false;
		if (!group.isActive()) return false;
		if (group.isControlPanel()) return false;
		if (group.isDepot()) return false;
		if (group.isLayoutPrototype()) return false;
		if (group.isLayout()) return false;
		if (group.isLayoutSetPrototype()) return false;
		if (group.isUserPersonalSite()) return false;
		if (group.isUser()) return false;
		if (group.isUserGroup()) return false;
		if (group.isOrganization()) return false;
		if (group.getFriendlyURL().equalsIgnoreCase("/global")) return false;
		
		long companyId = group.getCompanyId();

		if (!PortalPermissionUtil.contains(permissionChecker, ActionKeys.VIEW_CONTROL_PANEL)) return false;
		
		if (permissionChecker.isOmniadmin()) return true;
		
		if (permissionChecker.isCompanyAdmin(companyId)) return true;
		
		if (permissionChecker.isGroupAdmin(group.getGroupId())) return true;
		
		return super.isShow(permissionChecker, group);
	}	
}