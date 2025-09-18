package com.mw.site.crawler;


import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.config.InfraConfigTO;
import com.mw.site.crawler.config.SitePageCrawlerConfiguration;
import com.mw.site.crawler.config.SitePageCrawlerInfraConfiguration;
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
	property = {
		"osgi.command.scope=sitePageLinkCrawler", 
		"osgi.command.function=crawlPagesAsGuest",
		"osgi.command.function=crawlPagesAsUser"
	},
	configurationPid = {
		SitePageCrawlerConfiguration.PID,
		SitePageCrawlerInfraConfiguration.PID,
	},
	service = SitePageLinkCrawler.class
)
public class SitePageLinkCrawler {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("Activating...");
		
		_sitePageCrawlerConfiguration = ConfigurableUtil.createConfigurable(SitePageCrawlerConfiguration.class, properties);	
		_sitePageCrawlerInfraConfiguration = ConfigurableUtil.createConfigurable(SitePageCrawlerInfraConfiguration.class, properties);
		
		_log.info("outputFolder: " + _sitePageCrawlerInfraConfiguration.outputFolder());
		_log.info("objectDefinitionERC: " + _sitePageCrawlerInfraConfiguration.objectDefinitionERC());
		_log.info("pageBodySelector: " + _sitePageCrawlerInfraConfiguration.pageBodySelector());
		_log.info("crawlerUserAgent: " + _sitePageCrawlerInfraConfiguration.crawlerUserAgent());
		_log.info("connectTimeout: " + _sitePageCrawlerInfraConfiguration.connectTimeout());
		_log.info("connectionRequestTimeout: " + _sitePageCrawlerInfraConfiguration.connectionRequestTimeout());
		_log.info("socketTimeout: " + _sitePageCrawlerInfraConfiguration.socketTimeout());
		_log.info("maximumRedirects: " + _sitePageCrawlerInfraConfiguration.maximumRedirects());
	}
	
    /**
     * Used by web
     */	
	public ResponseTO crawlPagesWeb(ConfigTO config, long companyId, Group group, String origin, User user, Locale locale, LayoutCrawler layoutCrawler, List<Layout> layouts) {
		_log.info("starting crawlPagesWeb");
		
		_log.info("CompanyId: " + companyId);
		_log.info("Site GroupId: " + group.getGroupId());
		_log.info("Locale: " + locale);
		_log.info("Origin: " + origin);
		_log.info("SitePublicUrlPrefix: " + layoutCrawler.getSitePublicUrlPrefix());
		_log.info("SitePrivateUrlPrefix: " + layoutCrawler.getSitePrivateUrlPrefix());
		
		return crawlPages(config, origin, user, locale, group, layoutCrawler, layouts);
	}

    /**
     * Used by gogo shell sitePageHTMLCrawler:crawlPagesAsUser
     */   	
	public void crawlPagesAsUser(String companyIdString, String siteIdString, String origin, String emailAddress, String loginIdEnc, String passwordEnc, String cookieDomain) {
		_log.info("starting crawlPagesAsUser");
		
		long companyId = Long.valueOf(companyIdString);
		long siteId = Long.valueOf(siteIdString);
		
		_log.info("CompanyId: " + companyId);
		_log.info("Site GroupId: " + siteId);
		_log.info("Origin: " + origin);
		_log.info("EmailAddress: " + emailAddress);
		_log.info("LoginIdEnc: " + loginIdEnc);
		_log.info("PasswordEnc: " + passwordEnc);
		_log.info("CookieDomain: " + cookieDomain);
		
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
		
		// Always current users locale for synchronous
		Locale locale = user.getLocale();
		
		_log.info("Locale: " + locale);
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			log("Site not found for siteId: " + siteId, false);
			
			return;
		}
		
		//The synchronous always uses the System Settings...
		InfraConfigTO infraConfig = getInfraConfiguration();
		ConfigTO config = getDefaultConfiguration(false, false);
		
		LayoutCrawler layoutCrawler = new LayoutCrawler(companyId, siteId, infraConfig, origin, loginIdEnc, passwordEnc, cookieDomain, locale);
		
		_log.info("SitePublicUrlPrefix: " + layoutCrawler.getSitePublicUrlPrefix());
		_log.info("SitePrivateUrlPrefix: " + layoutCrawler.getSitePrivateUrlPrefix());
		
		List<Layout> layouts = getPages(config, siteId, false);
		
		crawlPages(config, origin, user, locale, group, layoutCrawler, layouts);
	}
	
    /**
     * Used by gogo shell sitePageHTMLCrawler:crawlPagesAsGuest
     */  	
	public void crawlPagesAsGuest(String companyIdString, String siteIdString, String origin, String cookieDomain) {
		_log.info("starting crawlPagesAsGuest");
		
		long companyId = Long.valueOf(companyIdString);
		long siteId = Long.valueOf(siteIdString);
		
		_log.info("CompanyId: " + companyId);
		_log.info("Site GroupId: " + siteId);
		_log.info("Origin: " + origin);
		_log.info("CookieDomain: " + cookieDomain);
		
		Company company = companyLocalService.fetchCompany(companyId);
		
		if (company == null) {
			log("Company not found for companyId: " + companyId, false);
			
			return;
		}
		
		User guestUser = userLocalService.fetchGuestUser(companyId);
		
		// Always current users locale for synchronous
		Locale locale = guestUser.getLocale();
		
		_log.info("Locale: " + locale);
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			log("Site not found for siteId: " + siteId, false);
			
			return;
		}
		
		//The synchronous always uses the System Settings...
		InfraConfigTO infraConfig = getInfraConfiguration();
		ConfigTO config = getDefaultConfiguration(true, false); // Running from Gogo Shell, this always uses Guest Locale...

		LayoutCrawler layoutCrawler = new LayoutCrawler(companyId, siteId, infraConfig, origin, cookieDomain, guestUser, locale);
		
		_log.info("SitePublicUrlPrefix: " + layoutCrawler.getSitePublicUrlPrefix());
		
		List<Layout> layouts = getPages(config, siteId, false);
		
		crawlPages(config, origin, guestUser, locale, group, layoutCrawler, layouts);
	}	

	private ResponseTO crawlPages(ConfigTO config, String origin, User user, Locale locale, Group group, LayoutCrawler layoutCrawler, List<Layout> layouts) {
		
		long guestRoleId = getGuestRoleId(user.getCompanyId());
		
		try {
			boolean hasLayouts = false;
			long pageBodySelectorNotFoundCount = 0;
			
			if (!layouts.isEmpty()) hasLayouts = true;
					
			List<PageTO> pageTOs = new ArrayList<PageTO>();
			if (layoutCrawler != null) {
				for (Layout layout: layouts) {
					if (!pageTOs.isEmpty() && pageTOs.size() % 50 == 0) { // Show progress...
						if (layoutCrawler.isAsynchronous()) {
							log("Asynchronous Site Page Crawler still running in Site " + group.getName(locale) + " for " + user.getFullName(), layoutCrawler.isAsynchronous());
						} else {
							log("Site Page Crawler still running in Site " + group.getName(locale) + " for " + user.getFullName(), layoutCrawler.isAsynchronous());
						}
					}
					
					if (!isCrawlableLayout(config, layout)) continue;
					
					PageTO pageTO = new PageTO();
					
					pageTO.setName(layout.getName(locale));
					pageTO.setPrivatePage(layout.isPrivateLayout());
					pageTO.setHiddenPage(layout.isHidden());
					
					if ((!layout.isPrivateLayout() && config.isCheckPageGuestRoleViewPermission()) || config.isRunAsGuestUser()) {
						int hasGuestViewPermission = hasGuestViewPermission(guestRoleId, layout);
						
						// Skip if run as guest user and Guest Role doesn't have View permission for the page.
						if (config.isRunAsGuestUser() && hasGuestViewPermission != 1) continue;
					
						pageTO.setGuestRoleViewPermissionEnabled(hasGuestViewPermission);
					}
					
					List<Element> webContentArticles = new ArrayList<Element>();
					List<Element> links = new ArrayList<Element>();

					String[] responseArray = layoutCrawler.getLayoutContent(layout, locale);
					
					if (Validator.isNotNull(responseArray) && Validator.isNotNull(responseArray[0]) && Validator.isNotNull(responseArray[1])) {
						String pageURL = responseArray[0];
						String pageHtml = responseArray[1];
									
						pageTO.setUrl(pageURL);
						
						Document htmlDocument = Jsoup.parse(pageHtml.toString());

						// <section id="content"> or similar...
						Element body = htmlDocument.selectFirst(_sitePageCrawlerInfraConfiguration.pageBodySelector());

						if (Validator.isNull(body)) {
							_log.info(pageTO.getName() + ": element body is null.");
							
							pageBodySelectorNotFoundCount++;
						} else {
							if (config.isWebContentDisplayWidgetLinksOnly()) {
								// <div class="journal-content-article " .....
								webContentArticles = body.select("div.journal-content-article").asList();
								
								// Get all links inside the WCM Articles
								for (Element webContentArticle: webContentArticles) {
									links.addAll(webContentArticle.select("a").asList());
								}
							} else { // All from the page body
								links.addAll(body.select("a").asList());
							}
						}
						
						List<LinkTO> linkTOs = new ArrayList<LinkTO>();
						
						long validLinkCount = 0;
						long invalidLinkCount = 0;
						long skippedExternalLinkCount = 0;
						long skippedPrivateLinkCount = 0;
						long loginRequiredLinkCount = 0;
						long unexpectedExternalRedirectLinkCount = 0;
						
						if (!links.isEmpty()) {
							for (Element link: links) {
								if (includeLink(link)) {
									String href = link.attr("href");
									String label = link.text();
									String[] linkStatus = {"", ""};
									
									if (config.isValidateLinksOnPages()) {
										linkStatus = layoutCrawler.validateLink(href, locale, config.isSkipExternalLinks(), config.isRunAsGuestUser());
										
										if (Validator.isNotNull(linkStatus) && linkStatus[0].equalsIgnoreCase("" + HttpStatus.SC_OK)) { //200
											validLinkCount += 1;
										} else if (Validator.isNotNull(linkStatus) && (linkStatus[0].equalsIgnoreCase("" + LinkTO.SKIPPED_EXTERNAL_LINK_STATUS_CODE))) {
											skippedExternalLinkCount += 1;
										} else if (Validator.isNotNull(linkStatus) && (linkStatus[0].equalsIgnoreCase("" + LinkTO.SKIPPED_PRIVATE_PAGE_STATUS_CODE))) {
											skippedPrivateLinkCount += 1;												
										} else if (Validator.isNotNull(linkStatus) && (linkStatus[0].equalsIgnoreCase("" + LinkTO.LOGIN_REDIRECT_STATUS_CODE))) {
											loginRequiredLinkCount += 1;
										} else if (Validator.isNotNull(linkStatus) && (linkStatus[0].equalsIgnoreCase("" + LinkTO.UNEXPECTED_EXTERNAL_REDIRECT_STATUS_CODE))) {
											unexpectedExternalRedirectLinkCount += 1;										
										} else {
											invalidLinkCount += 1;
										}							
									}
									
									linkTOs.add(new LinkTO(href, label, linkStatus[0], linkStatus[1]));
								}
							}					
						}
						
						if (config.isValidateLinksOnPages()) {
							pageTO.setValidLinkCount(validLinkCount);
							pageTO.setInvalidLinkCount(invalidLinkCount);	
							pageTO.setSkippedExternalLinkCount(skippedExternalLinkCount);	
							pageTO.setSkippedPrivateLinkCount(skippedPrivateLinkCount);	
							pageTO.setLoginRequiredLinkCount(loginRequiredLinkCount);
							pageTO.setUnexpectedExternalRedirectLinkCount(unexpectedExternalRedirectLinkCount);
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
					
					log(message, layoutCrawler.isAsynchronous());
				} else {
					message = "No Pages found. Ensure that the Crawler settings were correct.";
					
					log(message, layoutCrawler.isAsynchronous());	
				}
				
				return new ResponseTO(false, null, message, 0);
			} else if (pageBodySelectorNotFoundCount > 0 && pageBodySelectorNotFoundCount >= pageTOs.size()) {
				String message = "";
				
				message = "No Pages crawled - check that the Crawler settings were correct, in particular the Page Body Selector value.";
				
				log(message, layoutCrawler.isAsynchronous());
				
				return new ResponseTO(false, null, message, 0);
			}
			
			File outputFolderFile = new File(_sitePageCrawlerInfraConfiguration.outputFolder());
			if (!outputFolderFile.exists()) outputFolderFile.mkdirs();

			String fileName = "sitePageLinks_" + group.getName(locale) + "_" + locale.toString() + "_" + System.currentTimeMillis() + ".txt";
			String outputFilePath = outputFolderFile.getAbsolutePath() + "/" + fileName;

			Path normalizedOutputFilePath = Paths.get(outputFilePath).normalize();
			
			boolean fileGenerated = outputToTxtFile(config, group.getName(locale), locale.toString(), pageTOs, normalizedOutputFilePath.toString(), layoutCrawler);
			
			if (fileGenerated) {
				log("Done, Output written to: " + normalizedOutputFilePath, layoutCrawler.isAsynchronous());
			} else {
				return new ResponseTO(false, null, "Output file not generated.", 0);
			}
			
			return new ResponseTO(true, normalizedOutputFilePath.toString(), null, pageTOs.size());
		} catch (Exception e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
			
			return new ResponseTO(false, null, "Exception occurred: " + e.getClass() + ": " + e.getMessage(), 0);
		}
	}
	
	public List<Layout> getPages(ConfigTO config, long groupId, boolean asynchronous) {
		List<Layout> layouts = new ArrayList<Layout>();

		if (config.isIncludePublicPages()) {
			List<Layout> publicLayouts = layoutLocalService.getLayouts(groupId, false);	
			
			log("Total Public Page Count (before filtering performed): " + publicLayouts.size(), asynchronous);
			
			layouts.addAll(publicLayouts);
		}
		
		if (config.isIncludePrivatePages()) {
			List<Layout> privateLayouts = layoutLocalService.getLayouts(groupId, true);
			
			log("Total Private Page Count (before filtering performed): " + privateLayouts.size(), asynchronous);
			
			layouts.addAll(privateLayouts);
		}

		return layouts;
	}	

	private boolean isCrawlableLayout(ConfigTO config, Layout layout) {
		if (Validator.isNull(layout)) return false;
		if (Validator.isNull(layout.getType())) return false;
		
		if (!config.isIncludeHiddenPages()) {
			if (layout.isHidden()) return false;	
		}
		
		if (layout.isDraft() || layout.isDraftLayout()) return false;
		if (!layout.isApproved()) return false;
		if (layout.isInactive() || layout.isIncomplete()) return false;
		if (layout.isExpired()) return false;
		
		if (layout.getType().equalsIgnoreCase("content") || layout.getType().equalsIgnoreCase("portlet")) {
			return true;
		}
	
		return false;
	}
	
	private boolean outputToTxtFile(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, String outputFilePath, LayoutCrawler layoutCrawler) {
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(outputFilePath);
			
			long pageCount = 1;
			long totalLinkCount = 0;
			long totalValidLinkCount = 0;
			long totalInvalidLinkCount = 0;
			long totalSkippedExternalLinkCount = 0;
			long totalSkippedPrivateLinkCount = 0;
			long totalLoginRequiredLinkCount = 0;
			long totalUnexpectedExternalRedirectLinkCount = 0;
			
			if (layoutCrawler.isAsynchronous()) {
				printWriter.println("Trigger: Site Page Crawler Widget");
			} else {
				if (config.isRunAsGuestUser()) {
					printWriter.println("Trigger: crawlPagesAsGuest GoGo Shell Command");
				} else {
					printWriter.println("Trigger: crawlPagesAsUser GoGo Shell Command");
				}
			}
			
			printWriter.println("Site Name: " + siteName);
			printWriter.println("Hostname: " + layoutCrawler.getOrigin());
			if (config.isRunAsGuestUser()) {
				if (config.isUseCurrentUsersLocaleWhenRunAsGuestUser()) {
					printWriter.println("Locale: " + localeString + " (from Current User)");
				} else {
					printWriter.println("Locale: " + localeString + " (from Guest User)");
				}
			} else {
				printWriter.println("Locale: " + localeString);	
			}
			printWriter.println("Web Content Display Widget Links Only: " + getLabel(config.isWebContentDisplayWidgetLinksOnly()));
			printWriter.println("Run as Guest User: " + getLabel(config.isRunAsGuestUser()));
			if (config.isRunAsGuestUser()) {
				printWriter.println("Use Current Users Locale when Run as Guest User: " + getLabel(config.isUseCurrentUsersLocaleWhenRunAsGuestUser()));
				printWriter.println("Include Public Pages: Yes (Overridden)");
				printWriter.println("Include Private Pages: No (Overridden)");
			} else {
				printWriter.println("Include Public Pages: " + getLabel(config.isIncludePublicPages()));
				printWriter.println("Include Private Pages: " + getLabel(config.isIncludePrivatePages()));				
			}
			printWriter.println("Include Hidden Pages: " + getLabel(config.isIncludeHiddenPages()));
			if (config.isIncludePublicPages()) {
				printWriter.println("Check Public Page Guest Role View Permission: " + getLabel(config.isCheckPageGuestRoleViewPermission()));	
			}
			printWriter.println("Validate Links on Pages: " + getLabel(config.isValidateLinksOnPages()));
			if (config.isValidateLinksOnPages()) {
				printWriter.println("Validate Links on Pages > Skip Links using any other hostname: " + getLabel(config.isSkipExternalLinks()));
			}
			printWriter.println("");
			printWriter.println("Page Count: " + pageTOs.size());
			printWriter.println("");
			
			for (PageTO pageTO: pageTOs) {
				boolean pageHasLinks = false;
				if (Validator.isNotNull(pageTO.getLinks()) && !pageTO.getLinks().isEmpty()) {
					pageHasLinks = true;
				}
				
				printWriter.println("**********************************************************************");
				printWriter.println("[" + pageCount + "] Page Name: " + pageTO.getName());
				printWriter.println("[" + pageCount + "] Page URL: " + pageTO.getUrl());
				if (pageTO.isPrivatePage()) {
					printWriter.println("[" + pageCount + "] Page Type: Private Page");
				} else {
					printWriter.println("[" + pageCount + "] Page Type: Public Page");	
				}

				if (config.isIncludeHiddenPages()) {
					printWriter.println("[" + pageCount + "] Hidden Page: " + getLabel(pageTO.isHiddenPage()));	
				}
				
				if (!pageTO.isPrivatePage() && config.isCheckPageGuestRoleViewPermission()) {
					printWriter.println("[" + pageCount + "] Public Page Guest Role View Permission Enabled: " + getLabel(pageTO.getGuestRoleViewPermissionEnabled()));
				}
				
				if (pageHasLinks) {
					printWriter.println("[" + pageCount + "] Page Link Count: " + pageTO.getLinks().size());
					
					totalLinkCount += pageTO.getLinks().size();
				} else {
					printWriter.println("[" + pageCount + "] Page Link Count: 0");
				}
				
				if (config.isValidateLinksOnPages() && pageHasLinks) {
					printWriter.println("[" + pageCount + "] Valid Link Count: " + pageTO.getValidLinkCount());
					printWriter.println("[" + pageCount + "] Invalid Link Count: " + pageTO.getInvalidLinkCount());
					printWriter.println("[" + pageCount + "] Skipped Other Hostname Link Count: " + pageTO.getSkippedExternalLinkCount());
					printWriter.println("[" + pageCount + "] Skipped Private Link Count: " + pageTO.getSkippedPrivateLinkCount());
					printWriter.println("[" + pageCount + "] Login Required Link Count: " + pageTO.getLoginRequiredLinkCount());
					printWriter.println("[" + pageCount + "] Unexpected External Redirect Link Count: " + pageTO.getUnexpectedExternalRedirectLinkCount());
					
					totalValidLinkCount += pageTO.getValidLinkCount();
					totalInvalidLinkCount += pageTO.getInvalidLinkCount();
					totalSkippedExternalLinkCount += pageTO.getSkippedExternalLinkCount();
					totalSkippedPrivateLinkCount += pageTO.getSkippedPrivateLinkCount();
					totalLoginRequiredLinkCount += pageTO.getLoginRequiredLinkCount();
					totalUnexpectedExternalRedirectLinkCount += pageTO.getUnexpectedExternalRedirectLinkCount();
							
				}
				printWriter.println("**********************************************************************");
				printWriter.println("");
				
				List<LinkTO> linkTOs = pageTO.getLinks();
				
				long linkCount = 1;
				
				if (pageHasLinks) {
					for (LinkTO linkTO: linkTOs) {
						printWriter.println("[" + pageCount + "-" + linkCount + "] Link Label: " + linkTO.getLabel());
						printWriter.println("[" + pageCount + "-" + linkCount + "] Link URL: " + linkTO.getHref());
						if (config.isValidateLinksOnPages()) {
							if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + HttpStatus.SC_OK)) { //200
								printWriter.println("[" + pageCount + "-" + linkCount + "] Link appears to be valid.");
							} else if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + LinkTO.SKIPPED_EXTERNAL_LINK_STATUS_CODE)) {
								printWriter.println("[" + pageCount + "-" + linkCount + "] " + linkTO.getStatusMessage());
							} else if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + LinkTO.SKIPPED_PRIVATE_PAGE_STATUS_CODE)) {
								printWriter.println("[" + pageCount + "-" + linkCount + "] " + linkTO.getStatusMessage());
							} else if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + LinkTO.LOGIN_REDIRECT_STATUS_CODE)) {
								printWriter.println("[" + pageCount + "-" + linkCount + "] " + linkTO.getStatusMessage());
							} else if (Validator.isNotNull(linkTO.getStatusCode()) && linkTO.getStatusCode().equalsIgnoreCase("" + LinkTO.UNEXPECTED_EXTERNAL_REDIRECT_STATUS_CODE)) {
								printWriter.println("[" + pageCount + "-" + linkCount + "] " + linkTO.getStatusMessage());						
							} else {
								if (Validator.isNotNull(linkTO.getStatusMessage())) {
									printWriter.println("[" + pageCount + "-" + linkCount + "] Link not verified: " + linkTO.getStatusCode() + ", " + linkTO.getStatusMessage());
								} else {
									printWriter.println("[" + pageCount + "-" + linkCount + "] Link not verified: " + linkTO.getStatusCode());	
								}
							}
						}
						
						linkCount ++;
						
						printWriter.println("");				
					}					
				} else {
					printWriter.println("No links found on the page.");
					printWriter.println("");
				}
				
				pageCount ++;
			}
			
			printWriter.println("**********************************************************************");
			printWriter.println("");
			printWriter.println("Total Link Count: " + totalLinkCount);
			
			if (config.isValidateLinksOnPages()) {
				printWriter.println("Total Valid Link Count: " + totalValidLinkCount);
				printWriter.println("Total Invalid Link Count: " + totalInvalidLinkCount);
				if (config.isSkipExternalLinks()) {
					printWriter.println("Total Skipped Other Hostname Link Count: " + totalSkippedExternalLinkCount);
				}
				if (config.isRunAsGuestUser()) {
					printWriter.println("Total Skipped Private Link Count: " + totalSkippedPrivateLinkCount);
				}
				printWriter.println("Total Login Required Link Count Link Count: " + totalLoginRequiredLinkCount);
				printWriter.println("Total Unexpected Other Hostname Redirect Link Count: " + totalUnexpectedExternalRedirectLinkCount);
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
		
		// Prevent infinite loop in case the widget is added to a real page...
		if (href.toLowerCase().indexOf("p_p_id=com_mw_crawler_web_CrawlerPortlet".toLowerCase()) >= 0) return false;

		return true;
	}
	
	private long getGuestRoleId(long companyId) {
		Role role = roleLocalService.fetchRole(companyId, RoleConstants.GUEST);
		
		if (Validator.isNotNull(role)) return role.getRoleId();
		
		return -1;
	}
	
	private String getLabel(int value) {
		if (value == -1) return "Unknown";
		if (value == 0) return "No";
		if (value == 1) return "Yes";
		
		return "Unknown";
 	}
	
	private String getLabel(boolean value) {
		if (value) return "Yes";
		
		return "No";
 	}
	
	private int hasGuestViewPermission(long guestRoleId, Layout layout) {
		if (guestRoleId == -1 || Validator.isNull(layout)) return -1;
		
		String resourceName = Layout.class.getName();
		String primKey = String.valueOf(layout.getPlid());

		try {
			boolean hasGuestViewPermission = resourcePermissionLocalService.hasResourcePermission(
			    layout.getCompanyId(),
			    resourceName,
			    ResourceConstants.SCOPE_INDIVIDUAL,
			    primKey,
			    guestRoleId,
			    ActionKeys.VIEW
			);
			
			if (hasGuestViewPermission) {
				return 1;
			} else {
				return 0;
			}
		} catch (PortalException e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		}
		
		return -1;
	}
	
	private void log(String output, boolean asynchronous) {
		_log.info(output);
		
		if (!asynchronous) {
			System.out.println(output);
		}
	}
	
	public InfraConfigTO getInfraConfiguration() {
		InfraConfigTO infraConfig = new InfraConfigTO(_sitePageCrawlerInfraConfiguration.crawlerUserAgent(), _sitePageCrawlerInfraConfiguration.connectTimeout(), _sitePageCrawlerInfraConfiguration.connectionRequestTimeout(), _sitePageCrawlerInfraConfiguration.socketTimeout(), _sitePageCrawlerInfraConfiguration.maximumRedirects());

		return  infraConfig;
	}
	
	public ConfigTO getDefaultConfiguration(boolean runAsGuestUser, boolean useCurrentUsersLocaleWhenRunAsGuestUser) {
		
		boolean includePublicPages = _sitePageCrawlerConfiguration.includePublicPages();
		boolean includePrivatePages = _sitePageCrawlerConfiguration.includePrivatePages();
		
		if (runAsGuestUser) {
			includePublicPages = true;
			includePrivatePages = false;
		} else {
			useCurrentUsersLocaleWhenRunAsGuestUser = false;
		}
		
		ConfigTO config = new ConfigTO(_sitePageCrawlerConfiguration.webContentDisplayWidgetLinksOnly(), runAsGuestUser, useCurrentUsersLocaleWhenRunAsGuestUser, includePublicPages, includePrivatePages, _sitePageCrawlerConfiguration.includeHiddenPages(), _sitePageCrawlerConfiguration.checkPageGuestRoleViewPermission(), _sitePageCrawlerConfiguration.validateLinksOnPages(), _sitePageCrawlerConfiguration.skipExternalLinks());
		
		return config;
	}
	
	public ConfigTO getDefaultConfiguration() {
		return getDefaultConfiguration(_sitePageCrawlerConfiguration.runAsGuestUser(), _sitePageCrawlerConfiguration.useCurrentUsersLocaleWhenRunAsGuestUser());
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
	private RoleLocalService roleLocalService;
	
	@Reference
	private ResourcePermissionLocalService resourcePermissionLocalService;
	
    @Reference
    private Portal _portal;
    
	private volatile SitePageCrawlerConfiguration _sitePageCrawlerConfiguration;
	
	private volatile SitePageCrawlerInfraConfiguration _sitePageCrawlerInfraConfiguration;
	
	private static final Log _log = LogFactoryUtil.getLog(SitePageLinkCrawler.class);	
}