package com.mw.site.crawler.util;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.VirtualHost;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.kernel.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.model.VirtualHostTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CrawlerUtil {
	
	public static String getSiteFriendlyURL(long siteGroupId) {
		Group siteGroup = GroupLocalServiceUtil.fetchGroup(siteGroupId);
		
		if (Validator.isNotNull(siteGroup)) return siteGroup.getFriendlyURL();
		
		return "";
	}

	public static String getSitePublicUrlPrefix(String origin, String siteFriendlyUrl) {
		
		String publicPrefix = PortalUtil.getPathFriendlyURLPublic();
		
		String publicLayoutUrlPrefix = origin + publicPrefix + siteFriendlyUrl;
		
		return publicLayoutUrlPrefix;
	}
	
	public static String getSitePrivateUrlPrefix(String origin, String siteFriendlyUrl) {
		
        String privatePrefix = PortalUtil.getPathFriendlyURLPrivateGroup();
		
		String privateLayoutUrlPrefix = origin + privatePrefix + siteFriendlyUrl;
		
		return privateLayoutUrlPrefix;
	}
	
	public static String getOrigin(ThemeDisplay themeDisplay) {
		
		String protocol = themeDisplay.getPortalURL().split(":")[0]; // http or https
        String host = themeDisplay.getServerName();
        int port = themeDisplay.getServerPort();
		
		String prefix = protocol + "://" + host;
		if (port != 80 && port != 443) prefix += ":" + port;
		
		return prefix;
	}
	
	public static Locale getSiteDefaultLocale(long siteGroupId) {
		try {
			return PortalUtil.getSiteDefaultLocale(siteGroupId);
		} catch (PortalException e) {
			_log.info("Error calling PortalUtil.getSiteDefaultLocale: " + e.getMessage());
		}
		
		return null;
	}
	
	public static List<VirtualHostTO> getCurrentSiteVirtualHosts(long siteGroupId, String defaultLanguageId) {
		List<VirtualHostTO> virtualHosts = new ArrayList<VirtualHostTO>();
		
    	List<VirtualHost> liferayVirtualHosts = VirtualHostLocalServiceUtil.getVirtualHosts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
    	
    	for (VirtualHost liferayVirtualHost: liferayVirtualHosts) {
    		LayoutSet layoutSet = LayoutSetLocalServiceUtil.fetchLayoutSet(liferayVirtualHost.getLayoutSetId());

    		if (Validator.isNotNull(layoutSet) && layoutSet.getGroupId() == siteGroupId) {
    			String languageId = liferayVirtualHost.getLanguageId();
    			
    			boolean defaultLanguage = false;
    			
    			//If it uses 'Default Language' on Site Settings > Site Configuration > Site URL > Language
    			if (Validator.isNull(liferayVirtualHost.getLanguageId())) {
    				languageId = defaultLanguageId;
    				
    				defaultLanguage = true;
    			} else if (liferayVirtualHost.getLanguageId().equalsIgnoreCase(defaultLanguageId)) { //Explictly set...
    				defaultLanguage = true;
    			}
    			
        		VirtualHostTO virtualHost = new VirtualHostTO(liferayVirtualHost.getCompanyId(), layoutSet.getGroupId(), true, liferayVirtualHost.getHostname(), !layoutSet.isPrivateLayout(), languageId, defaultLanguage);
        		
        		virtualHosts.add(virtualHost);
    		}
    	}		
    	
    	virtualHosts.sort(Comparator.comparing(VirtualHostTO::getHostName, String.CASE_INSENSITIVE_ORDER));    	
		
    	return virtualHosts;
	}
	
	public static List<VirtualHostTO> getAllVirtualHosts(String defaultLanguageId) {
		List<VirtualHostTO> virtualHosts = new ArrayList<VirtualHostTO>();
		
    	List<VirtualHost> liferayVirtualHosts = VirtualHostLocalServiceUtil.getVirtualHosts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
    	
    	for (VirtualHost liferayVirtualHost: liferayVirtualHosts) {
    		LayoutSet layoutSet = LayoutSetLocalServiceUtil.fetchLayoutSet(liferayVirtualHost.getLayoutSetId());

    		if (Validator.isNotNull(layoutSet)) {
    			String languageId = liferayVirtualHost.getLanguageId();
    			
    			boolean defaultLanguage = false;
    			
    			//If it uses 'Default Language' on Site Settings > Site Configuration > Site URL > Language
    			if (Validator.isNull(liferayVirtualHost.getLanguageId())) {
    				languageId = defaultLanguageId;
    				
    				defaultLanguage = true;
    			} else if (liferayVirtualHost.getLanguageId().equalsIgnoreCase(defaultLanguageId)) { //Explictly set...
    				defaultLanguage = true;
    			}

        		VirtualHostTO virtualHost = new VirtualHostTO(liferayVirtualHost.getCompanyId(), layoutSet.getGroupId(), true, liferayVirtualHost.getHostname(), !layoutSet.isPrivateLayout(), languageId, defaultLanguage);
        		
        		virtualHosts.add(virtualHost);
    		} else {
    			boolean defaultLanguage = false;
    			
    			Locale locale = LocaleUtil.getDefault();
    			
    			if (locale.toString().equalsIgnoreCase(defaultLanguageId)) defaultLanguage = true;
    			
        		VirtualHostTO virtualHost = new VirtualHostTO(liferayVirtualHost.getCompanyId(), 0, false, liferayVirtualHost.getHostname(), false, locale.toString(), defaultLanguage);
        		
        		virtualHosts.add(virtualHost);
    		}
    	}
    	
    	virtualHosts.sort(Comparator.comparing(VirtualHostTO::getHostName, String.CASE_INSENSITIVE_ORDER));    	
		
    	return virtualHosts;
	}	
	
	public static VirtualHostTO isCurrentSiteVirtualHost(List<VirtualHostTO> virtualHosts, String host) {
		
		if (Validator.isNull(virtualHosts) || virtualHosts.isEmpty()) return null;
    	
    	for (VirtualHostTO virtualHost: virtualHosts) {
    		if (virtualHost.getHostName().equalsIgnoreCase(host)) return virtualHost;
    	}
		
    	return null;
	}	
	
	public static boolean isPrivateSiteVirtualHost(List<VirtualHostTO> virtualHosts, String host) {
		
		if (Validator.isNull(virtualHosts) || virtualHosts.isEmpty() || Validator.isNull(host)) return false;
    	
    	for (VirtualHostTO virtualHost: virtualHosts) {
    		if (virtualHost.getHostName().equalsIgnoreCase(host) && virtualHost.isSiteVirtualHost() && !virtualHost.isPublicVirtualHost()) return true;
    	}
		
    	return false;
	}	
		
	
	public static VirtualHostTO getSiteVirtualHostByTypeByLanguageId(List<VirtualHostTO> virtualHosts, boolean isPublicVirtualHost, String languageId) {
		
		for (VirtualHostTO virtualHost: virtualHosts) {
			if (virtualHost.getLanguageId().equalsIgnoreCase(languageId)) {
				if (virtualHost.isPublicVirtualHost() == isPublicVirtualHost) {
					return virtualHost;
				}
			}
		}
		
    	return null;
	}
	
 	private static final Log _log = LogFactoryUtil.getLog(CrawlerUtil.class);		
}