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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		property = {"osgi.command.function=crawlPrivatePages", "osgi.command.scope=sitePageHTMLCrawler"},
		service = SitePageHTMLCrawler.class
	)
public class SitePageHTMLCrawler {

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
			_log.info("Company not found for companyId: " + companyId);
			System.out.println("Company not found for companyId: " + companyId);
			
			return;
		}
		
		User user = userLocalService.fetchUserByEmailAddress(companyId, emailAddress);
		
		if (user == null) {
			_log.info("User not found for emailAddress: " + emailAddress);
			System.out.println("User not found for emailAddress: " + emailAddress);
			
			return;
		}
		
		Group group = groupLocalService.fetchGroup(siteId);
		
		if (group == null || !group.isSite()) {
			_log.info("Site not found for siteId: " + siteId);
			System.out.println("Site not found for siteId: " + siteId);
			
			return;
		}
		
		List<Layout> layouts = layoutLocalService.getLayouts(siteId, true);
		
		_log.info("Private Page Count: " + layouts.size());
		System.out.println("Private Page Count: " + layouts.size());
		
		PrivateLayoutCrawler layoutCrawler = new PrivateLayoutCrawler(layoutUrlPrefix, emailAddressEnc, passwordEnc, cookieDomain);
		
		String folderName = "/siteExport_" + System.currentTimeMillis();
		
		File outputFolder = new File(outputBaseFolder + folderName);
		if (!outputFolder.exists()) outputFolder.mkdirs();
		
		_log.info("Output location: " + outputFolder.getAbsolutePath());
		System.out.println("Output location: " + outputFolder.getAbsolutePath());
		
		for (Layout layout: layouts) {
			String[] responseArray = crawlPageContent(layout, layoutCrawler, user.getLocale());
			if (responseArray != null) {
				String layoutContent = responseArray[0];	
				
				PrintWriter printWriter = null;

				try {
					printWriter = new PrintWriter(outputFolder.getAbsolutePath() + "/" + layout.getFriendlyURL().replaceAll("/", "_") + ".html");
					
					printWriter.println(layoutContent);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (printWriter != null) printWriter.close();
					
					printWriter.close();
				}				
			}
		}
		
		_log.info("Done...");
		System.out.println("Done...");
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
	
	private static final Log _log = LogFactoryUtil.getLog(SitePageHTMLCrawler.class);	
}