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

import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		property = {"osgi.command.function=crawlPages", "osgi.command.scope=sitePageLinkCrawler"},
		service = SitePageLinkCrawler.class
	)
public class SitePageLinkCrawler {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("activating");
	}

	public void crawlPages(String companyIdString, String siteIdString, String validateLinksOnPages, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String emailAddress, String emailAddressEnc, String passwordEnc, String cookieDomain, String outputFolder) {
		
		long companyId = Long.valueOf(companyIdString);
		long siteId = Long.valueOf(siteIdString);
		boolean validateLinksOnPagesBoolean = Boolean.valueOf(validateLinksOnPages);
		
		_log.info("CompanyId: " + companyId);
		_log.info("SiteId: " + siteId);
		_log.info("validateLinksOnPagesBoolean: " + validateLinksOnPagesBoolean);
		_log.info("relativeUrlPrefix: " + relativeUrlPrefix);
		_log.info("PublicLayoutURLPrefix: " + publicLayoutUrlPrefix);
		_log.info("PrivateLayoutURLPrefix: " + privateLayoutUrlPrefix);
		_log.info("EmailAddress: " + emailAddress);
		_log.info("EmailAddressEnc: " + emailAddressEnc);
		_log.info("PasswordEnc: " + passwordEnc);
		_log.info("CookieDomain: " + cookieDomain);
		_log.info("OutputFolder: " + outputFolder);
		
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
		
		List<Layout> publicLayouts = layoutLocalService.getLayouts(siteId, false);
		List<Layout> privateLayouts = layoutLocalService.getLayouts(siteId, true);
		
		log("Public Page Count: " + publicLayouts.size());
		log("Private Page Count: " + privateLayouts.size());
		
		List<Layout> layouts = new ArrayList<Layout>();
		
		layouts.addAll(publicLayouts);
		layouts.addAll(privateLayouts);
		
		boolean hasLayouts = false;
		
		if (!layouts.isEmpty()) hasLayouts = true;
		
		LayoutCrawler layoutCrawler = new LayoutCrawler(publicLayoutUrlPrefix, privateLayoutUrlPrefix, emailAddressEnc, passwordEnc, cookieDomain, user.getLocale());
		
		List<PageTO> pageTOs = new ArrayList<PageTO>();
		
		for (Layout layout: layouts) {
			PageTO pageTO = new PageTO();
			
			pageTO.setName(layout.getName(user.getLocale()));
			pageTO.setPrivatePage(layout.isPrivateLayout());

			String[] responseArray = layoutCrawler.getLayoutContent(layout, user.getLocale());
			
			if (Validator.isNotNull(responseArray) && Validator.isNotNull(responseArray[0]) && Validator.isNotNull(responseArray[1])) {
				String pageURL = responseArray[0];
				String pageHtml = responseArray[1];
							
				pageTO.setUrl(pageURL);
				
				Document htmlDocument = Jsoup.parse(pageHtml.toString());

				Element body = htmlDocument.selectFirst("section#content"); // Ensure this is valid if using a custom theme

				List<Element> links = body.select("a").asList();
				
				List<LinkTO> linkTOs = new ArrayList<LinkTO>();
				
				long validLinkCount = 0;
				long invalidLinkCount = 0;
				
				for (Element link: links) {
					if (includeLink(link)) {
						String href = link.attr("href");
						String label = link.text();
						String[] linkStatus = {"", ""};
						
						if (validateLinksOnPagesBoolean) {
							linkStatus = layoutCrawler.validateLink(href, relativeUrlPrefix, user.getLocale());
							
							if (Validator.isNotNull(linkStatus) && linkStatus[0].equalsIgnoreCase("" + HttpStatus.SC_OK)) { //200
								validLinkCount += 1;
							} else {
								invalidLinkCount += 1;
							}							
						}
						
						linkTOs.add(new LinkTO(href, label, linkStatus[0], linkStatus[1]));
					}
				}
				
				if (validateLinksOnPagesBoolean) {
					pageTO.setValidLinkCount(validLinkCount);
					pageTO.setInvalidLinkCount(invalidLinkCount);	
				}
				
				pageTO.setLinks(linkTOs);

				pageTOs.add(pageTO);
			}
		}
		
		if (pageTOs.isEmpty()) {
			if (hasLayouts) {
				log("No Pages crawled - check the logs for errors...");		
			} else {
				log("No Pages found.");	
			}
			
			return;
		}
		
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists()) outputFolderFile.mkdirs();
		
		String fileName = "sitePageLinks_" + group.getName(user.getLocale()) + "_" + user.getLocale().toString() + "_" + System.currentTimeMillis() + ".txt";

		outputToTxtFile(validateLinksOnPagesBoolean, pageTOs, outputFolderFile, fileName);
		
		log("Done, Output written to: " + outputFolderFile.getAbsolutePath() + "/" + fileName);
	}

	private void outputToTxtFile(boolean validateLinksOnPagesBoolean, List<PageTO> pageTOs, File outputFolderFile,
			String fileName) {
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(outputFolderFile.getAbsolutePath() + "/" + fileName);
			
			long pageCount = 1;
			
			for (PageTO pageTO: pageTOs) {
				printWriter.println("**********************************************************************");
				printWriter.println("[" + pageCount + "] Page Name: " + pageTO.getName());
				printWriter.println("[" + pageCount + "] Page URL: " + pageTO.getUrl());
				printWriter.println("[" + pageCount + "] Private Page: " + pageTO.isPrivatePage());
				printWriter.println("[" + pageCount + "] Page Link Count: " + pageTO.getLinks().size());
				
				if (validateLinksOnPagesBoolean) {	
					if (pageTO.getLinks().size() > 0) {
						printWriter.println("[" + pageCount + "] Valid Link Count: " + pageTO.getValidLinkCount());
						printWriter.println("[" + pageCount + "] Invalid Link Count: " + pageTO.getInvalidLinkCount());						
					}
				}
				printWriter.println("**********************************************************************");
				printWriter.println("");
				
				List<LinkTO> linkTOs = pageTO.getLinks();
				
				if (!linkTOs.isEmpty()) {
					for (LinkTO linkTO: linkTOs) {
						printWriter.println("Link Label: " + linkTO.getLabel());
						printWriter.println("Link URL: " + linkTO.getHref());
						if (validateLinksOnPagesBoolean) {
							if (linkTO.getStatusCode().equalsIgnoreCase("" + HttpStatus.SC_OK)) { //200
								printWriter.println("Link appears to be valid.");
							} else {
								if (Validator.isNotNull(linkTO.getStatusMessage())) {
									printWriter.println("Link not verified: " + linkTO.getStatusCode() + ", " + linkTO.getStatusMessage());
								} else {
									printWriter.println("Link not verified: " + linkTO.getStatusCode());	
								}
							}
						}
						printWriter.println("");					
					}					
				} else {
					printWriter.println("No links found on the page.");
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