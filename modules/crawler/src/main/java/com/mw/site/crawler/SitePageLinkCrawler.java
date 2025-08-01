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
import com.mw.site.crawler.model.ResponseTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
		if (_log.isInfoEnabled()) _log.info("Activating...");
	}
	
	public ResponseTO crawlPage(long companyId, Group group, boolean validateLinksOnPages, String relativeUrlPrefix, User user, String outputFolder, LayoutCrawler layoutCrawler, List<Layout> layouts) {
		_log.info("starting crawlPages asynchronous");
		
		_log.info("CompanyId: " + companyId);
		_log.info("GroupId: " + group.getGroupId());
		_log.info("validateLinksOnPages: " + validateLinksOnPages);
		_log.info("relativeUrlPrefix: " + relativeUrlPrefix);
		_log.info("OutputFolder: " + outputFolder);
		
		return crawlPages(relativeUrlPrefix, outputFolder, validateLinksOnPages, user, group, layoutCrawler, layouts, true);
	}

	public void crawlPages(String companyIdString, String siteIdString, String validateLinksOnPages, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String emailAddress, String loginIdEnc, String passwordEnc, String cookieDomain, String outputFolder) {
		_log.info("starting crawlPages synchronous");
		
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
		_log.info("LoginIdEnc: " + loginIdEnc);
		_log.info("PasswordEnc: " + passwordEnc);
		_log.info("CookieDomain: " + cookieDomain);
		_log.info("OutputFolder: " + outputFolder);
		
		Company company = companyLocalService.fetchCompany(companyId);
		
		if (company == null) {
			log("Company not found for companyId: " + companyId, false);
			
			return;
		}
		
		User user = userLocalService.fetchUserByEmailAddress(companyId, emailAddress);
		
		if (user == null) {
			log("User not found for emailAddress: " + emailAddress, false);
			
			return;
		}
		
		_log.info("Locale: " + user.getLocale());
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			log("Site not found for siteId: " + siteId, false);
			
			return;
		}
		
		LayoutCrawler layoutCrawler = new LayoutCrawler(publicLayoutUrlPrefix, privateLayoutUrlPrefix, loginIdEnc, passwordEnc, cookieDomain, user.getLocale());
		
		List<Layout> layouts = getPages(siteId, false);
		
		crawlPages(relativeUrlPrefix, outputFolder, validateLinksOnPagesBoolean, user, group, layoutCrawler, layouts, false);
	}
	
	public List<Layout> getPages(long groupId, boolean asynchronous) {
		
		List<Layout> publicLayouts = layoutLocalService.getLayouts(groupId, false);
		List<Layout> privateLayouts = layoutLocalService.getLayouts(groupId, true);
		
		log("Public Page Count: " + publicLayouts.size(), asynchronous);
		log("Private Page Count: " + privateLayouts.size(), asynchronous);
		
		List<Layout> layouts = new ArrayList<Layout>();
		
		layouts.addAll(publicLayouts);
		layouts.addAll(privateLayouts);		
		
		return layouts;
	}

	private ResponseTO crawlPages(String relativeUrlPrefix, String outputFolder,
			boolean validateLinksOnPagesBoolean, User user, Group group, LayoutCrawler layoutCrawler, List<Layout> layouts, boolean asynchronous) {
		
		try {
			boolean hasLayouts = false;
			
			if (!layouts.isEmpty()) hasLayouts = true;
					
			List<PageTO> pageTOs = new ArrayList<PageTO>();
			if (layoutCrawler != null) {
				for (Layout layout: layouts) {
					if (pageTOs.size() % 50 == 0) {
						if (asynchronous) {
							log("Asynchronous Site Page Crawler still running in Site " + group.getName(user.getLocale()) + " for " + user.getFullName(), asynchronous);
						} else {
							log("Site Page Crawler still running in Site " + group.getName(user.getLocale()) + " for " + user.getFullName(), asynchronous);
						}
					}
					
					if (!isCrawlableLayout(layout)) continue; // Skip if not a content page or widget page...
					
					PageTO pageTO = new PageTO();
					
					pageTO.setName(layout.getName(user.getLocale()));
					pageTO.setPrivatePage(layout.isPrivateLayout());
					
					List<Element> webContentArticles = new ArrayList<Element>();
					List<Element> links = new ArrayList<Element>();

					String[] responseArray = layoutCrawler.getLayoutContent(layout, user.getLocale());
					
					if (Validator.isNotNull(responseArray) && Validator.isNotNull(responseArray[0]) && Validator.isNotNull(responseArray[1])) {
						String pageURL = responseArray[0];
						String pageHtml = responseArray[1];
									
						pageTO.setUrl(pageURL);
						
						Document htmlDocument = Jsoup.parse(pageHtml.toString());

						// <section id="content"> or similar...
						Element body = htmlDocument.selectFirst("section#content"); // Ensure this is valid if using a custom theme

						if (Validator.isNull(body)) {
							_log.info(pageTO.getName() + ": element body is null.");
						} else {
							// <div class="journal-content-article " .....
							webContentArticles = body.select("div.journal-content-article").asList();
						}
						
						// Get all links inside the WCM Articles
						for (Element webContentArticle: webContentArticles) {
							links.addAll(webContentArticle.select("a").asList());
						}
						
						List<LinkTO> linkTOs = new ArrayList<LinkTO>();
						
						long validLinkCount = 0;
						long invalidLinkCount = 0;
						
						if (!links.isEmpty()) {
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
						}
						
						if (validateLinksOnPagesBoolean) {
							pageTO.setValidLinkCount(validLinkCount);
							pageTO.setInvalidLinkCount(invalidLinkCount);	
						}
						
						pageTO.setLinks(linkTOs);

						pageTOs.add(pageTO);
					}
				}
			}
			
			if (pageTOs.isEmpty()) {
				String message = "";
				
				if (hasLayouts) {
					message = "No Pages crawled - check the logs for errors and ensure that the Crawler settings were correct.";
					
					log(message, asynchronous);		
				} else {
					message = "No Pages found. Ensure that the Crawler settings were correct.";
					
					log(message, asynchronous);	
				}
				
				return new ResponseTO(false, null, message, 0);
			}
			
			File outputFolderFile = new File(outputFolder);
			if (!outputFolderFile.exists()) outputFolderFile.mkdirs();
			
			String fileName = "sitePageLinks_" + group.getName(user.getLocale()) + "_" + user.getLocale().toString() + "_" + System.currentTimeMillis() + ".txt";
			String outputFilePath = outputFolderFile.getAbsolutePath() + "/" + fileName;

			Path normalizedOutputFilePath = Paths.get(outputFilePath).normalize();
			
			boolean fileGenerated = outputToTxtFile(validateLinksOnPagesBoolean, pageTOs, normalizedOutputFilePath.toString());
			
			if (fileGenerated) {
				log("Done, Output written to: " + normalizedOutputFilePath, asynchronous);
			} else {
				return new ResponseTO(false, null, "Output file not generated.", 0);
			}
			
			return new ResponseTO(true, normalizedOutputFilePath.toString(), null, pageTOs.size());
		} catch (Exception e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
			
			return new ResponseTO(false, null, "Exception occurred: " + e.getClass() + ": " + e.getMessage(), 0);
		}
	}

	private boolean isCrawlableLayout(Layout layout) {
		if (Validator.isNull(layout)) return false;
		if (Validator.isNull(layout.getType())) return false;
		
		if (layout.isHidden()) return false;
		if (layout.isDraft() || layout.isDraftLayout()) return false;
		if (!layout.isApproved()) return false;
		if (layout.isInactive() || layout.isIncomplete()) return false;
		if (layout.isExpired()) return false;
		
		if (layout.getType().equalsIgnoreCase("content") || layout.getType().equalsIgnoreCase("portlet")) {
			return true;
		}
	
		return false;
	}
	
	private boolean outputToTxtFile(boolean validateLinksOnPagesBoolean, List<PageTO> pageTOs, String outputFilePath) {
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(outputFilePath);
			
			long pageCount = 1;
			
			for (PageTO pageTO: pageTOs) {
				boolean pageHasLinks = false;
				if (Validator.isNotNull(pageTO.getLinks()) && !pageTO.getLinks().isEmpty()) {
					pageHasLinks = true;
				}
				
				printWriter.println("**********************************************************************");
				printWriter.println("[" + pageCount + "] Page Name: " + pageTO.getName());
				printWriter.println("[" + pageCount + "] Page URL: " + pageTO.getUrl());
				printWriter.println("[" + pageCount + "] Private Page: " + pageTO.isPrivatePage());
			
				if (pageHasLinks) {
					printWriter.println("[" + pageCount + "] Page Link Count: " + pageTO.getLinks().size());
				} else {
					printWriter.println("[" + pageCount + "] Page Link Count: 0");
				}				
				
				if (validateLinksOnPagesBoolean && pageHasLinks) {
					printWriter.println("[" + pageCount + "] Valid Link Count: " + pageTO.getValidLinkCount());
					printWriter.println("[" + pageCount + "] Invalid Link Count: " + pageTO.getInvalidLinkCount());
				}
				printWriter.println("**********************************************************************");
				printWriter.println("");
				
				List<LinkTO> linkTOs = pageTO.getLinks();
				
				if (pageHasLinks) {
					for (LinkTO linkTO: linkTOs) {
						printWriter.println("Link Label: " + linkTO.getLabel());
						printWriter.println("Link URL: " + linkTO.getHref());
						if (validateLinksOnPagesBoolean) {
							if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + HttpStatus.SC_OK)) { //200
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
			
			return true;
			
		} catch (FileNotFoundException e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		} catch (Exception e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		}
		finally {
			if (printWriter != null) printWriter.close();
			
			printWriter.close();
		}
		
		return false;
	}
	
	private boolean includeLink(Element link) {
		String label = link.text();
		String href = link.attr("href");
	
		// Skip if label is empty
		if (Validator.isNull(label) || Validator.isNull(href)) return false;
		
		if (href.equalsIgnoreCase("javascript:void(0);")) return false;
		
		if (href.toLowerCase().startsWith("javascript")) return false;
		
		if (href.toLowerCase().startsWith("mailto:")) return false;
		
		if (href.toLowerCase().startsWith("file://")) return false;
		
		if (href.toLowerCase().startsWith("tel:")) return false;
		
		if (href.toLowerCase().startsWith("#")) return false; // Skip if anchor link to current page...
		
		if (href.toLowerCase().indexOf("/~/control_panel/manage".toLowerCase()) >= 0) return false;

		return true;
	}
	
	private void log(String output, boolean asynchronous) {
		_log.info(output);
		
		if (!asynchronous) {
			System.out.println(output);
		}
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