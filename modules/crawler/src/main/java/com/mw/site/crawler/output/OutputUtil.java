package com.mw.site.crawler.output;

import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.model.PageTO;

import java.util.ArrayList;
import java.util.List;

public class OutputUtil {
	
	
	public static List<SimpleOutputTO> getConfigOutput(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, LayoutCrawler layoutCrawler) {
		List<SimpleOutputTO> headings = new ArrayList<SimpleOutputTO>();
		
		if (layoutCrawler.isAsynchronous()) {
			headings.add(new SimpleOutputTO("Trigger", "Site Page Crawler Widget"));
		} else {
			if (config.isRunAsGuestUser()) {
				headings.add(new SimpleOutputTO("Trigger", "crawlPagesAsGuest GoGo Shell Command"));
			} else {
				headings.add(new SimpleOutputTO("Trigger", "crawlPagesAsUser GoGo Shell Command"));
			}
		}
		
		headings.add(new SimpleOutputTO("Site Name", siteName));
		headings.add(new SimpleOutputTO("Hostname", layoutCrawler.getOrigin()));
		if (config.isRunAsGuestUser()) {
			if (config.isUseCurrentUsersLocaleWhenRunAsGuestUser()) {
				headings.add(new SimpleOutputTO("Locale", localeString + " (from Current User)"));
			} else {
				headings.add(new SimpleOutputTO("Locale", localeString + " (from Guest User)"));
			}
		} else {
			headings.add(new SimpleOutputTO("Locale", localeString));
		}
		headings.add(new SimpleOutputTO("Web Content Display Widget Links Only", OutputUtil.getLabel(config.isWebContentDisplayWidgetLinksOnly())));
		headings.add(new SimpleOutputTO("Run as Guest User", OutputUtil.getLabel(config.isRunAsGuestUser())));
		if (config.isRunAsGuestUser()) {
			headings.add(new SimpleOutputTO("Use Current Users Locale when Run as Guest User", OutputUtil.getLabel(config.isUseCurrentUsersLocaleWhenRunAsGuestUser())));
			headings.add(new SimpleOutputTO("Include Public Pages", "Yes (Overridden)"));
			headings.add(new SimpleOutputTO("Include Private Pages", "No (Overridden)"));
		} else {
			headings.add(new SimpleOutputTO("Include Public Pages", OutputUtil.getLabel(config.isIncludePublicPages())));
			headings.add(new SimpleOutputTO("Include Private Pages", OutputUtil.getLabel(config.isIncludePrivatePages())));
		}
		headings.add(new SimpleOutputTO("Include Hidden Pages", OutputUtil.getLabel(config.isIncludeHiddenPages())));
		if (config.isIncludePublicPages()) {
			headings.add(new SimpleOutputTO("Check Public Page Guest Role View Permission", OutputUtil.getLabel(config.isCheckPageGuestRoleViewPermission())));
		}
		headings.add(new SimpleOutputTO("Validate Links on Pages", OutputUtil.getLabel(config.isValidateLinksOnPages())));
		if (config.isValidateLinksOnPages()) {
			headings.add(new SimpleOutputTO("Validate Links on Pages > Skip Links using any other hostname", OutputUtil.getLabel(config.isSkipExternalLinks())));
		}
		headings.add(new SimpleOutputTO("Page Count", pageTOs.size() + ""));
		
		return headings;
	}
	

	public static List<SimpleOutputTO> getSummaryOutput(ConfigTO config, long totalLinkCount,
			long totalValidLinkCount, long totalInvalidLinkCount, long totalSkippedExternalLinkCount,
			long totalSkippedPrivateLinkCount, long totalLoginRequiredLinkCount,
			long totalUnexpectedExternalRedirectLinkCount) {
		List<SimpleOutputTO> headings = new ArrayList<SimpleOutputTO>();
		
		headings.add(new SimpleOutputTO("Total Link Count", totalLinkCount + ""));
		
		if (config.isValidateLinksOnPages()) {
			headings.add(new SimpleOutputTO("Total Valid Link Count", totalValidLinkCount + ""));
			headings.add(new SimpleOutputTO("Total Invalid Link Count", totalInvalidLinkCount + ""));
			if (config.isSkipExternalLinks()) {
				headings.add(new SimpleOutputTO("Total Skipped Other Hostname Link Count", totalSkippedExternalLinkCount + ""));
			}
			if (config.isRunAsGuestUser()) {
				headings.add(new SimpleOutputTO("Total Skipped Private Link Count", totalSkippedPrivateLinkCount + ""));
			}
			headings.add(new SimpleOutputTO("Total Login Required Link Count Link Count", totalLoginRequiredLinkCount + ""));
			headings.add(new SimpleOutputTO("Total Unexpected Other Hostname Redirect Link Count", totalUnexpectedExternalRedirectLinkCount + ""));
		}
		
		return headings;
	}		
	
	public static String getLabel(int value) {
		if (value == -1) return "Unknown";
		if (value == 0) return "No";
		if (value == 1) return "Yes";
		
		return "Unknown";
 	}
	
	public static String getLabel(boolean value) {
		if (value) return "Yes";
		
		return "No";
 	}	
}