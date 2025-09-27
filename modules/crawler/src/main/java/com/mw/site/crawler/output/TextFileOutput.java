package com.mw.site.crawler.output;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.model.LinkTO;
import com.mw.site.crawler.model.PageTO;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class TextFileOutput {

	public boolean output(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, String outputFilePath, LayoutCrawler layoutCrawler) {
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
			
			List<SimpleOutputTO> headings = OutputUtil.getConfigOutput(config, siteName, localeString, pageTOs, layoutCrawler);
			
			for (SimpleOutputTO output: headings) {				
				if (output.isStoringLong()) {
					printWriter.println(output.getLabel() + ": " + output.getLongValue());
				} else {
					printWriter.println(output.getLabel() + ": " + output.getValue());
				}
			}
			
			printWriter.println("");
			
			for (PageTO pageTO: pageTOs) {
				boolean pageHasLinks = false;
				if (Validator.isNotNull(pageTO.getLinks()) && !pageTO.getLinks().isEmpty()) {
					pageHasLinks = true;
				}
				
				printWriter.println("**********************************************************************");
				printWriter.println("[" + pageCount + "] Page Name: " + pageTO.getName());
				printWriter.println("[" + pageCount + "] Page Friendly URL: " + pageTO.getFriendlyUrl());
				printWriter.println("[" + pageCount + "] Page URL: " + pageTO.getUrl());
				if (pageTO.isPrivatePage()) {
					printWriter.println("[" + pageCount + "] Page Type: Private Page");
				} else {
					printWriter.println("[" + pageCount + "] Page Type: Public Page");	
				}

				if (config.isIncludeHiddenPages()) {
					printWriter.println("[" + pageCount + "] Hidden Page: " + OutputUtil.getLabel(pageTO.isHiddenPage()));	
				}
				
				if (!pageTO.isPrivatePage() && config.isCheckPageGuestRoleViewPermission()) {
					printWriter.println("[" + pageCount + "] Public Page Guest Role View Permission Enabled: " + OutputUtil.getLabel(pageTO.getGuestRoleViewPermissionEnabled()));
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
							printWriter.println("[" + pageCount + "-" + linkCount + "] " + linkTO.getOutput());	
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
			
			List<SimpleOutputTO> footers = OutputUtil.getSummaryOutput(config, totalLinkCount, totalValidLinkCount, totalInvalidLinkCount,
					totalSkippedExternalLinkCount, totalSkippedPrivateLinkCount, totalLoginRequiredLinkCount,
					totalUnexpectedExternalRedirectLinkCount);
				
			for (SimpleOutputTO output: footers) {
				if (output.isStoringLong()) {
					printWriter.println(output.getLabel() + ": " + output.getLongValue());
				} else {
					printWriter.println(output.getLabel() + ": " + output.getValue());
				}
			}				

			return true;
			
		} catch (FileNotFoundException e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		} catch (Exception e) {
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		}
		finally {
			if (printWriter != null) printWriter.close();
		}
		
		return false;
	}
	
    private static final Log _log = LogFactoryUtil.getLog(TextFileOutput.class);    	
}