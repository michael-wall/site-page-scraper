package com.mw.site.crawler.output;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.model.LinkTO;
import com.mw.site.crawler.model.PageTO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelXLSXFileOutput {
	
	public boolean output(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, String outputFilePath, LayoutCrawler layoutCrawler) {
		XSSFWorkbook workbook = null;
		
        try {
        	workbook = new XSSFWorkbook();
        	
			long totalLinkCount = 0;
			long totalValidLinkCount = 0;
			long totalInvalidLinkCount = 0;
			long totalSkippedExternalLinkCount = 0;
			long totalSkippedPrivateLinkCount = 0;
			long totalLoginRequiredLinkCount = 0;
			long totalUnexpectedExternalRedirectLinkCount = 0;

        	XSSFSheet summarySheet = workbook.createSheet("Summary");
        	XSSFSheet pagesSheet = workbook.createSheet("Pages");
        	XSSFSheet linksSheet = workbook.createSheet("Links");
            
			List<SimpleOutputTO> headings = OutputUtil.getConfigOutput(config, siteName, localeString, pageTOs, layoutCrawler);
            
            // Create a header row
            XSSFRow summaryHeaderRow = summarySheet.createRow(0);
            summaryHeaderRow.createCell(0).setCellValue("Setting");
            summaryHeaderRow.createCell(1).setCellValue("Value");
            
            int summaryRowCount = 1;
            
			for (SimpleOutputTO output: headings) {
				XSSFRow summaryRow = summarySheet.createRow(summaryRowCount);
				
				summaryRow.createCell(0).setCellValue(output.getLabel());
				summaryRow.createCell(1).setCellValue(output.getValue());

				summaryRowCount ++;
			}
            
            int pagesRowCount = 1;
            
            XSSFRow pagesHeaderRow = pagesSheet.createRow(0);
            pagesHeaderRow.createCell(0).setCellValue("Name");
            pagesHeaderRow.createCell(1).setCellValue("URL");
            pagesHeaderRow.createCell(2).setCellValue("Public Page");
            
            if (config.isIncludeHiddenPages()) {
            	pagesHeaderRow.createCell(3).setCellValue("Hidden Page");
            }
            
            if (config.isCheckPageGuestRoleViewPermission()) {
                pagesHeaderRow.createCell(getDynamicColumnNumberX(config)).setCellValue("Public Page Guest Role View Permission Enabled");            	
            }
            
            pagesHeaderRow.createCell(getDynamicColumnNumberY(config)).setCellValue("Page Link Count");
            
            int z = getDynamicColumnNumberY(config);
            
            if (config.isValidateLinksOnPages()) {
	            pagesHeaderRow.createCell(z + 1).setCellValue("Valid Link Count");
	            pagesHeaderRow.createCell(z + 2).setCellValue("Invalid Link Count");
	            pagesHeaderRow.createCell(z + 3).setCellValue("Skipped Other Hostname Link Count");
	            pagesHeaderRow.createCell(z + 4).setCellValue("Skipped Private Link Count");
	            pagesHeaderRow.createCell(z + 5).setCellValue("Login Required Link Count");
	            pagesHeaderRow.createCell(z + 6).setCellValue("Unexpected External Redirect Link Count");
            }
            
			for (PageTO pageTO: pageTOs) {
				XSSFRow pagesRow = pagesSheet.createRow(pagesRowCount);
			
				pagesRow.createCell(0).setCellValue(pageTO.getName());
				pagesRow.createCell(1).setCellValue(pageTO.getUrl());
				pagesRow.createCell(2).setCellValue(OutputUtil.getLabel(!pageTO.isPrivatePage()));
				
				if (config.isIncludeHiddenPages()) {
					pagesRow.createCell(3).setCellValue(OutputUtil.getLabel(pageTO.isHiddenPage()));
				}
				
				if (config.isCheckPageGuestRoleViewPermission()) {
					if (!pageTO.isPrivatePage()) {
						pagesRow.createCell(getDynamicColumnNumberX(config)).setCellValue(OutputUtil.getLabel(pageTO.getGuestRoleViewPermissionEnabled()));
					} else {
						pagesRow.createCell(getDynamicColumnNumberX(config)).setCellValue("N/A");
					}
				}
				
				pagesRow.createCell(getDynamicColumnNumberY(config)).setCellValue(pageTO.getLinks().size());

				if (config.isValidateLinksOnPages()) {
					pagesRow.createCell(z + 1).setCellValue(pageTO.getValidLinkCount());
					pagesRow.createCell(z + 2).setCellValue(pageTO.getInvalidLinkCount());
					pagesRow.createCell(z + 3).setCellValue(pageTO.getSkippedExternalLinkCount());
					pagesRow.createCell(z + 4).setCellValue(pageTO.getSkippedPrivateLinkCount());
					pagesRow.createCell(z + 5).setCellValue(pageTO.getLoginRequiredLinkCount());
					pagesRow.createCell(z + 6).setCellValue(pageTO.getUnexpectedExternalRedirectLinkCount());

					totalValidLinkCount += pageTO.getValidLinkCount();
					totalInvalidLinkCount += pageTO.getInvalidLinkCount();
					totalSkippedExternalLinkCount += pageTO.getSkippedExternalLinkCount();
					totalSkippedPrivateLinkCount += pageTO.getSkippedPrivateLinkCount();
					totalLoginRequiredLinkCount += pageTO.getLoginRequiredLinkCount();
					totalUnexpectedExternalRedirectLinkCount += pageTO.getUnexpectedExternalRedirectLinkCount();			
				}
				
				pagesRowCount ++;
			}			        
			
            XSSFRow linksSheetHeaderRow = linksSheet.createRow(0);
            linksSheetHeaderRow.createCell(0).setCellValue("Page");
            linksSheetHeaderRow.createCell(1).setCellValue("Link Label");
            linksSheetHeaderRow.createCell(2).setCellValue("Link URL");
            
            if (config.isValidateLinksOnPages()) {
            	linksSheetHeaderRow.createCell(3).setCellValue("Link Status");
	        }
            
            int linksRowCount = 1;
            
            for (PageTO pageTO: pageTOs) {
            	List<LinkTO> linkTOs = pageTO.getLinks();
            	
            	for (LinkTO linkTO: linkTOs) {
                	XSSFRow linksRow = linksSheet.createRow(linksRowCount);
                	
                	linksRow.createCell(0).setCellValue(pageTO.getName());
                	linksRow.createCell(1).setCellValue(linkTO.getLabel());
                	linksRow.createCell(2).setCellValue(linkTO.getHref());
                	
                	if (config.isValidateLinksOnPages()) {
                		linksRow.createCell(3).setCellValue(linkTO.getOutput());
                	}
                	
                	linksRowCount ++;
            	}
            }
            
			List<SimpleOutputTO> footers = OutputUtil.getSummaryOutput(config, totalLinkCount, totalValidLinkCount, totalInvalidLinkCount,
					totalSkippedExternalLinkCount, totalSkippedPrivateLinkCount, totalLoginRequiredLinkCount,
					totalUnexpectedExternalRedirectLinkCount);
			
			for (SimpleOutputTO output: footers) {
				XSSFRow summaryRow = summarySheet.createRow(summaryRowCount);
				
				summaryRow.createCell(0).setCellValue(output.getLabel());
				summaryRow.createCell(1).setCellValue(output.getValue());

				summaryRowCount ++;
			}
	         
            //Autosize columns
            for (int i = 0; i < 3; i++) {
            	summarySheet.autoSizeColumn(i);
            }
            for (int i = 0; i < 12; i++) {
            	pagesSheet.autoSizeColumn(i);
            }
            for (int i = 0; i < 4; i++) {
            	linksSheet.autoSizeColumn(i);
            }            

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel file created: " + outputFilePath);
            
		} catch (Exception e) {
			System.out.println(e.getClass() + ": " + e.getMessage());
			
			_log.error(e.getClass() + ": " + e.getMessage(), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {}
			}
		}
		
		return false;
	}
	
	private int getDynamicColumnNumberX(ConfigTO config) {
		if (config.isIncludeHiddenPages()) return 4;
		
		return 3;
	}
	
	private int getDynamicColumnNumberY(ConfigTO config) {
		if (config.isIncludeHiddenPages() && config.isCheckPageGuestRoleViewPermission()) return 5;
		
		if (config.isIncludeHiddenPages()) return 4;
		
		return 3;
	}

    private static final Log _log = LogFactoryUtil.getLog(ExcelXLSXFileOutput.class);
}
