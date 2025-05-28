package com.mw.site.crawler;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.model.LinkTO;
import com.mw.site.crawler.model.PageTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		property = {"osgi.command.function=crawlPrivatePages", "osgi.command.scope=sitePageLinkCrawler"},
		service = SitePageLinkCrawler.class
	)
public class SitePageLinkCrawler {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("activating");
	}

	public void crawlPrivatePages(String companyIdString, String siteIdString, String layoutUrlPrefix, String emailAddress, String emailAddressEnc, String passwordEnc, String cookieDomain, String outputBaseFolder) {
		
		long companyId = Long.valueOf(companyIdString);
		long siteId = Long.valueOf(siteIdString);
		
		_log.info("CompanyId: " + companyId);
		_log.info("SiteId: " + siteId);
		_log.info("LayoutURLPrefix: " + layoutUrlPrefix);
		_log.info("EmailAddress: " + emailAddress);
		_log.info("EmailAddressEnc: " + emailAddressEnc);
		_log.info("PasswordEnc: " + passwordEnc);
		_log.info("CookieDomain: " + cookieDomain);
		_log.info("Output Base Folder: " + outputBaseFolder);
		
		Company company = companyLocalService.fetchCompany(companyId);
		
		if (company == null) {
			log("Company not found for companyId: " + companyId);
			
			return;
		}
		
		User user = userLocalService.fetchUserByEmailAddress(companyId, emailAddress);
		
		if (user == null) {
			log("User not found for emailAddress: " + emailAddress);
			
			return;
		}
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			log("Site not found for siteId: " + siteId);
			
			return;
		}
		
		List<Layout> layouts = layoutLocalService.getLayouts(siteId, true); // Private pages only
		
		log("Private Page Count: " + layouts.size());
		
		PrivateLayoutCrawler layoutCrawler = new PrivateLayoutCrawler(layoutUrlPrefix, emailAddressEnc, passwordEnc, cookieDomain);
		
		List<PageTO> pageTOs = new ArrayList<PageTO>();
		
		for (Layout layout: layouts) {
			PageTO pageTO = new PageTO();
			
			pageTO.setName(layout.getName(user.getLocale()));
			
			log("Page Name: " + layout.getName(user.getLocale()));	
			
			String[] responseArray = crawlPageContent(layout, layoutCrawler, user.getLocale());
			
			if (Validator.isNotNull(responseArray) && Validator.isNotNull(responseArray[0]) && Validator.isNotNull(responseArray[1])) {
				String pageURL = responseArray[0];
				String pageHtml = responseArray[1];
				
				log("Page URL: " + pageURL);
				
				pageTO.setUrl(pageURL);
				
				Document htmlDocument = Jsoup.parse(pageHtml.toString());

				Element body = htmlDocument.selectFirst("section#content"); 
				
				List<Element> links = body.select("a").asList();
				
				List<LinkTO> linkTOs = new ArrayList<LinkTO>();
				
				for (Element link: links) {
					if (includeLink(link)) {
						String href = link.attr("href");
						String label = link.text();
						
						linkTOs.add(new LinkTO(href, label));				
					}
				}
				
				pageTO.setLinks(linkTOs);
				
				pageTOs.add(pageTO);
			}
		}
		
		PrintWriter printWriter = null;
		
		if (pageTOs.isEmpty()) {
			log("No Private Pages found...");	
			
			return;
		}
		
		File outputFolder = new File(outputBaseFolder);
		if (!outputFolder.exists()) outputFolder.mkdirs();
		
		String fileName = "sitePageLinks_" + group.getName(user.getLocale()) + "_" + System.currentTimeMillis() + ".txt";

		try {
			printWriter = new PrintWriter(outputFolder.getAbsolutePath() + "/" + fileName);
			
			long pageCount = 1;
			
			for (PageTO pageTO: pageTOs) {
				printWriter.println("**********************************************************************");
				printWriter.println("[" + pageCount + "] Page: " + pageTO.getName());
				printWriter.println("[" + pageCount + "] URL: " + pageTO.getUrl());
				printWriter.println("**********************************************************************");
				printWriter.println("");
				
				List<LinkTO> linkTOs = pageTO.getLinks();
				
				for (LinkTO linkTO: linkTOs) {
					printWriter.println("Link Label: " + linkTO.getLabel());
					printWriter.println("Link URL: " + linkTO.getHref());
					printWriter.println("");					
				}
				
				pageCount += 1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) printWriter.close();
			
			printWriter.close();
		}			
		
		log("Done, Output written to: " + outputFolder.getAbsolutePath() + "/" + fileName);
	}
	
	private boolean includeLink(Element link) {
		String label = link.text();
		String href = link.attr("href");
	
		// Skip if label is empty
		if (Validator.isNull(label) || Validator.isNull(href)) return false;
		
		if (href.equalsIgnoreCase("javascript:void(0);")) return false;
		
		if (href.toLowerCase().indexOf("/~/control_panel/manage".toLowerCase()) >= 0) return false;

		return true;
	}
	
	private void log(String output) {
		_log.info(output);
		System.out.println(output);		
	}
	
    private String[] crawlPageContent(
            Layout layout, PrivateLayoutCrawler layoutCrawler, Locale locale) {

        return layoutCrawler.getLayoutContent(layout, locale);
    }
	
	@Reference
	private LayoutLocalService layoutLocalService;
	
	@Reference
	private CompanyLocalService companyLocalService;
	
	@Reference
	private GroupLocalService groupLocalService;
	
	@Reference
	private UserLocalService userLocalService;
	
    @Reference
    private Portal _portal;
	
	private static final Log _log = LogFactoryUtil.getLog(SitePageLinkCrawler.class);	
}