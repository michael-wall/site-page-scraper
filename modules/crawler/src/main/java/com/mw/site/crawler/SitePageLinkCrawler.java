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
import com.mw.site.crawler.config.SitePageCrawlerConfiguration;
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
		configurationPid = SitePageCrawlerConfiguration.PID,
		service = SitePageLinkCrawler.class
	)
public class SitePageLinkCrawler {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("Activating...");
		
		_sitePageCrawlerConfiguration = ConfigurableUtil.createConfigurable(SitePageCrawlerConfiguration.class, properties);
		
		_log.info("outputFolder: " + _sitePageCrawlerConfiguration.outputFolder());
		
		_log.info("objectDefinitionERC: " + _sitePageCrawlerConfiguration.objectDefinitionERC());
		
		_log.info("pageBodySelector: " + _sitePageCrawlerConfiguration.pageBodySelector());
	}
	
	public ResponseTO crawlPage(ConfigTO config, long companyId, Group group, String relativeUrlPrefix, User user, LayoutCrawler layoutCrawler, List<Layout> layouts) {
		_log.info("starting crawlPages asynchronous");
		
		_log.info("CompanyId: " + companyId);
		_log.info("GroupId: " + group.getGroupId());
		_log.info("relativeUrlPrefix: " + relativeUrlPrefix);
		
		return crawlPages(config, relativeUrlPrefix, user, group, layoutCrawler, layouts, true);
	}

	public void crawlPages(String companyIdString, String siteIdString, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String emailAddress, String loginIdEnc, String passwordEnc, String cookieDomain) {
		_log.info("starting crawlPages synchronous");
		
		long companyId = Long.valueOf(companyIdString);
		long siteId = Long.valueOf(siteIdString);
		
		_log.info("CompanyId: " + companyId);
		_log.info("SiteId: " + siteId);
		_log.info("relativeUrlPrefix: " + relativeUrlPrefix);
		_log.info("PublicLayoutURLPrefix: " + publicLayoutUrlPrefix);
		_log.info("PrivateLayoutURLPrefix: " + privateLayoutUrlPrefix);
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
		
		_log.info("Locale: " + user.getLocale());
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			log("Site not found for siteId: " + siteId, false);
			
			return;
		}
		
		//The synchronlous always uses the System Settings...
		ConfigTO config = getDefaultConfiguration();
		
		LayoutCrawler layoutCrawler = new LayoutCrawler(config, publicLayoutUrlPrefix, privateLayoutUrlPrefix, loginIdEnc, passwordEnc, cookieDomain, user.getLocale());
		
		List<Layout> layouts = getPages(config, siteId, false);
		
		crawlPages(config, relativeUrlPrefix, user, group, layoutCrawler, layouts, false);
	}
	
	public List<Layout> getPages(ConfigTO config, long groupId, boolean asynchronous) {
		List<Layout> layouts = new ArrayList<Layout>();

		if (config.isIncludePublicPages()) {
			List<Layout> publicLayouts = layoutLocalService.getLayouts(groupId, false);	
			
			log("Public Page Count: " + publicLayouts.size(), asynchronous);
			
			layouts.addAll(publicLayouts);
		}
		
		if (config.isIncludePrivatePages()) {
			List<Layout> privateLayouts = layoutLocalService.getLayouts(groupId, true);
			
			log("Private Page Count: " + privateLayouts.size(), asynchronous);
			
			layouts.addAll(privateLayouts);
		}

		return layouts;
	}

	private ResponseTO crawlPages(ConfigTO config, String relativeUrlPrefix, User user, Group group, LayoutCrawler layoutCrawler, List<Layout> layouts, boolean asynchronous) {
		
		long guestRoleId = getGuestRoleId(user.getCompanyId());
		
		try {
			boolean hasLayouts = false;
			long pageBodySelectorNotFoundCount = 0;
			
			if (!layouts.isEmpty()) hasLayouts = true;
					
			List<PageTO> pageTOs = new ArrayList<PageTO>();
			if (layoutCrawler != null) {
				for (Layout layout: layouts) {
					if (!pageTOs.isEmpty() && pageTOs.size() % 50 == 0) { // Show progress...
						if (asynchronous) {
							log("Asynchronous Site Page Crawler still running in Site " + group.getName(user.getLocale()) + " for " + user.getFullName(), asynchronous);
						} else {
							log("Site Page Crawler still running in Site " + group.getName(user.getLocale()) + " for " + user.getFullName(), asynchronous);
						}
					}
					
					if (!isCrawlableLayout(config, layout)) continue;
					
					PageTO pageTO = new PageTO();
					
					pageTO.setName(layout.getName(user.getLocale()));
					pageTO.setPrivatePage(layout.isPrivateLayout());
					pageTO.setHiddenPage(layout.isHidden());
					
					if (config.isCheckPageGuestRoleViewPermission()) {
						int hasGuestViewPermission = hasGuestViewPermission(guestRoleId, layout);
					
						pageTO.setGuestRoleViewPermissionEnabled(hasGuestViewPermission);
					}
					
					List<Element> webContentArticles = new ArrayList<Element>();
					List<Element> links = new ArrayList<Element>();

					String[] responseArray = layoutCrawler.getLayoutContent(layout, user.getLocale());
					
					if (Validator.isNotNull(responseArray) && Validator.isNotNull(responseArray[0]) && Validator.isNotNull(responseArray[1])) {
						String pageURL = responseArray[0];
						String pageHtml = responseArray[1];
									
						pageTO.setUrl(pageURL);
						
						Document htmlDocument = Jsoup.parse(pageHtml.toString());

						// <section id="content"> or similar...
						Element body = htmlDocument.selectFirst(_sitePageCrawlerConfiguration.pageBodySelector());

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
						
						if (!links.isEmpty()) {
							for (Element link: links) {
								if (includeLink(link)) {
									String href = link.attr("href");
									String label = link.text();
									String[] linkStatus = {"", ""};
									
									if (config.isValidateLinksOnPages()) {
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
						
						if (config.isValidateLinksOnPages()) {
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
			} else if (pageBodySelectorNotFoundCount > 0 && pageBodySelectorNotFoundCount >= pageTOs.size()) {
				String message = "";
				
				message = "No Pages crawled - check that the Crawler settings were correct, in particular the Page Body Selector value.";
				
				log(message, asynchronous);
				
				return new ResponseTO(false, null, message, 0);
			}
			
			File outputFolderFile = new File(_sitePageCrawlerConfiguration.outputFolder());
			if (!outputFolderFile.exists()) outputFolderFile.mkdirs();
			
			String fileName = "sitePageLinks_" + group.getName(user.getLocale()) + "_" + user.getLocale().toString() + "_" + System.currentTimeMillis() + ".txt";
			String outputFilePath = outputFolderFile.getAbsolutePath() + "/" + fileName;

			Path normalizedOutputFilePath = Paths.get(outputFilePath).normalize();
			
			boolean fileGenerated = outputToTxtFile(config, group.getName(user.getLocale()), user.getLocale().toString(), pageTOs, normalizedOutputFilePath.toString());
			
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
	
	private boolean outputToTxtFile(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, String outputFilePath) {
		PrintWriter printWriter = null;
		
		try {
			printWriter = new PrintWriter(outputFilePath);
			
			long pageCount = 1;
			
			printWriter.println("Site Name: " + siteName);
			printWriter.println("Locale: " + localeString);
			printWriter.println("Web Content Display Widget Links Only: " + getLabel(config.isWebContentDisplayWidgetLinksOnly()));
			printWriter.println("Include Public Pages: " + getLabel(config.isIncludePublicPages()));
			printWriter.println("Include Private Pages: " + getLabel(config.isIncludePrivatePages()));
			printWriter.println("Include Hidden Pages: " + getLabel(config.isIncludeHiddenPages()));
			printWriter.println("Check Page Guest Role View Permission: " + getLabel(config.isCheckPageGuestRoleViewPermission()));
			printWriter.println("Validate Links On Pages: " + getLabel(config.isValidateLinksOnPages()));			
			

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
				printWriter.println("[" + pageCount + "] Private Page: " + getLabel(pageTO.isPrivatePage()));

				if (config.isIncludeHiddenPages()) {
					printWriter.println("[" + pageCount + "] Hidden Page: " + getLabel(pageTO.isHiddenPage()));	
				}
				
				if (config.isCheckPageGuestRoleViewPermission()) {
					printWriter.println("[" + pageCount + "] Page Guest Role View Permission Enabled: " + getLabel(pageTO.getGuestRoleViewPermissionEnabled()));
				}
				
				if (pageHasLinks) {
					printWriter.println("[" + pageCount + "] Page Link Count: " + pageTO.getLinks().size());
				} else {
					printWriter.println("[" + pageCount + "] Page Link Count: 0");
				}				
				
				if (config.isValidateLinksOnPages() && pageHasLinks) {
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
						if (config.isValidateLinksOnPages()) {
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
	
	public ConfigTO getDefaultConfiguration() {
		ConfigTO config = new ConfigTO(_sitePageCrawlerConfiguration.webContentDisplayWidgetLinksOnly(), _sitePageCrawlerConfiguration.includePublicPages(), _sitePageCrawlerConfiguration.includePrivatePages(), _sitePageCrawlerConfiguration.includeHiddenPages(), _sitePageCrawlerConfiguration.checkPageGuestRoleViewPermission(), _sitePageCrawlerConfiguration.validateLinksOnPages());

		return config;
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
	
	private static final Log _log = LogFactoryUtil.getLog(SitePageLinkCrawler.class);	
}