package com.mw.site.crawler.output;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.LayoutCrawler;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.model.LinkTO;
import com.mw.site.crawler.model.PageTO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelXLSXFileOutput {
	
	public boolean output(ConfigTO config, String siteName, String localeString, List<PageTO> pageTOs, String outputFilePath, LayoutCrawler layoutCrawler) {
		XSSFWorkbook workbook = null;
		
        try {
        	workbook = new XSSFWorkbook();
        	
        	XSSFFont headingFont = workbook.createFont();
        	headingFont.setBold(true);
        	
        	XSSFCellStyle headingCellStyle = getHeadingCellStyle(workbook, headingFont);        	
        	XSSFCellStyle subHeadingCellStyle = getSubHeadingCellStyle(workbook, headingFont);
        	XSSFCellStyle firstLinkRowCellStyle = getFirstLinkRowCellStyle(workbook, headingFont);
        	
			long totalLinkCount = 0;
			long totalValidLinkCount = 0;
			long totalInvalidLinkCount = 0;
			long totalSkippedExternalLinkCount = 0;
			long totalSkippedPrivateLinkCount = 0;
			long totalLoginRequiredLinkCount = 0;
			long totalUnexpectedExternalRedirectLinkCount = 0;

        	XSSFSheet summarySheet = workbook.createSheet("Summary");
        	XSSFSheet pagesSheet = workbook.createSheet("Pages");
        	XSSFSheet pageLinksSheet = workbook.createSheet("Page Links");
            
			List<SimpleOutputTO> headings = OutputUtil.getConfigOutput(config, siteName, localeString, pageTOs, layoutCrawler);
            
            // Create a header row
            XSSFRow summaryHeaderRow = summarySheet.createRow(0);
            
            summaryHeaderRow.createCell(0).setCellValue("Setting");
            summaryHeaderRow.getCell(0).setCellStyle(headingCellStyle);
            
            summaryHeaderRow.createCell(1).setCellValue("Value");
            summaryHeaderRow.getCell(1).setCellStyle(headingCellStyle);
            
            int summaryRowCount = 1;
            
			for (SimpleOutputTO output: headings) {
				XSSFRow summaryRow = summarySheet.createRow(summaryRowCount);
				
				summaryRow.createCell(0).setCellValue(output.getLabel());
				
				if (output.isStoringLong()) {
					summaryRow.createCell(1).setCellValue(output.getLongValue());	
				} else {
					summaryRow.createCell(1).setCellValue(output.getValue());
				}

				summaryRowCount ++;
			}
            
            int pagesRowCount = 1;
            
            int pageHeaderColumnIndex = 0;
            
            XSSFRow pagesHeaderRow = pagesSheet.createRow(0);
            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Page Name");
            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            
            pageHeaderColumnIndex++;
            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Page Friendly URL");
            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            
            pageHeaderColumnIndex++;
            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Page Full URL");
            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            
            if (config.isIncludePublicPages() && config.isIncludePrivatePages()) {
                pageHeaderColumnIndex++;
                pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Page Type");
                pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);            	
            }
            
            if (config.isIncludeHiddenPages()) {
            	pageHeaderColumnIndex++;
            	pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Hidden Page");
            	pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            }
            
            if (config.isCheckPageGuestRoleViewPermission()) {
            	pageHeaderColumnIndex++;
                pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Public Page Guest Role View Permission Enabled");
                pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            }
            
            pageHeaderColumnIndex++;
            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Page Link Count");
            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
           
            if (config.isValidateLinksOnPages()) {
            	pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Valid Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
	            
	            pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Invalid Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
	            
	            pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Skipped Other Hostname Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
	            
	            pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Skipped Private Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
	            
	            pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Login Required Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
	            
	            pageHeaderColumnIndex++;
	            pagesHeaderRow.createCell(pageHeaderColumnIndex).setCellValue("Unexpected External Redirect Link Count");
	            pagesHeaderRow.getCell(pageHeaderColumnIndex).setCellStyle(headingCellStyle);
            }
            
			for (PageTO pageTO: pageTOs) {
				XSSFRow pagesRow = pagesSheet.createRow(pagesRowCount);
				
				int pageRowColumnIndex = 0;
			
				pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getName());
				
				pageRowColumnIndex++;
				pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getFriendlyUrl());
				
				pageRowColumnIndex++;
				pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getUrl());
				
				if (config.isIncludePublicPages() && config.isIncludePrivatePages()) {
					String pageTypeLabel = "Private";
					if (!pageTO.isPrivatePage()) {
						pageTypeLabel = "Public";
					}
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTypeLabel);	 
				}
				
				if (config.isIncludeHiddenPages()) {
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(OutputUtil.getLabel(pageTO.isHiddenPage()));
				}
				
				if (config.isCheckPageGuestRoleViewPermission()) {
					if (!pageTO.isPrivatePage()) {
						pageRowColumnIndex++;
						pagesRow.createCell(pageRowColumnIndex).setCellValue(OutputUtil.getLabel(pageTO.getGuestRoleViewPermissionEnabled()));
					} else {
						pageRowColumnIndex++;
						pagesRow.createCell(pageRowColumnIndex).setCellValue("N/A");
					}
				}
				
				pageRowColumnIndex++;
				pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getLinks().size());

				if (config.isValidateLinksOnPages()) {
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getValidLinkCount());
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getInvalidLinkCount());
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getSkippedExternalLinkCount());
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getSkippedPrivateLinkCount());
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getLoginRequiredLinkCount());
					
					pageRowColumnIndex++;
					pagesRow.createCell(pageRowColumnIndex).setCellValue(pageTO.getUnexpectedExternalRedirectLinkCount());

					totalValidLinkCount += pageTO.getValidLinkCount();
					totalInvalidLinkCount += pageTO.getInvalidLinkCount();
					totalSkippedExternalLinkCount += pageTO.getSkippedExternalLinkCount();
					totalSkippedPrivateLinkCount += pageTO.getSkippedPrivateLinkCount();
					totalLoginRequiredLinkCount += pageTO.getLoginRequiredLinkCount();
					totalUnexpectedExternalRedirectLinkCount += pageTO.getUnexpectedExternalRedirectLinkCount();			
				}
				
				pagesRowCount ++;
			}
			
            XSSFRow linksSheetHeaderRow = pageLinksSheet.createRow(0);
            
            linksSheetHeaderRow.createCell(0).setCellValue("Source Page Name");
            linksSheetHeaderRow.getCell(0).setCellStyle(headingCellStyle);
            
            linksSheetHeaderRow.createCell(1).setCellValue("Source Page Friendly URL");
            linksSheetHeaderRow.getCell(1).setCellStyle(headingCellStyle);
            
            linksSheetHeaderRow.createCell(2).setCellValue("Page Link Number");
            linksSheetHeaderRow.getCell(2).setCellStyle(headingCellStyle);
            
            linksSheetHeaderRow.createCell(3).setCellValue("Link Label");
            linksSheetHeaderRow.getCell(3).setCellStyle(headingCellStyle);
            
            linksSheetHeaderRow.createCell(4).setCellValue("Link URL");
            linksSheetHeaderRow.getCell(4).setCellStyle(headingCellStyle);
            
            if (config.isValidateLinksOnPages()) {
            	linksSheetHeaderRow.createCell(5).setCellValue("HTTP Status Code");
            	linksSheetHeaderRow.getCell(5).setCellStyle(headingCellStyle);
            	
            	linksSheetHeaderRow.createCell(6).setCellValue("Link Status Message");
            	linksSheetHeaderRow.getCell(6).setCellStyle(headingCellStyle);
	        }
            
            int linksRowCount = 1;
            
            for (PageTO pageTO: pageTOs) {            	
            	if (Validator.isNotNull(pageTO.getLinks()) && !pageTO.getLinks().isEmpty()) {
                	List<LinkTO> linkTOs = pageTO.getLinks();
                	
                	totalLinkCount += pageTO.getLinks().size();
                	
                	int pageLinkCount = 1;
                	
                	for (LinkTO linkTO: linkTOs) {
                    	XSSFRow linksRow = pageLinksSheet.createRow(linksRowCount);
                    	
                    	//if (pageLinkCount == 1) { // Only populate for the first link of each page...
                        linksRow.createCell(0).setCellValue(pageTO.getName());
                        if (pageLinkCount == 1) linksRow.getCell(0).setCellStyle(firstLinkRowCellStyle);
                        	
                        linksRow.createCell(1).setCellValue(pageTO.getFriendlyUrl());
                        if (pageLinkCount == 1) linksRow.getCell(1).setCellStyle(firstLinkRowCellStyle);
                    	//}
                    	
                    	linksRow.createCell(2).setCellValue(pageLinkCount);
                    	if (pageLinkCount == 1) linksRow.getCell(2).setCellStyle(firstLinkRowCellStyle);
                    	
                    	linksRow.createCell(3).setCellValue(linkTO.getLabel());
                    	if (pageLinkCount == 1) linksRow.getCell(3).setCellStyle(firstLinkRowCellStyle);
                    	
                    	linksRow.createCell(4).setCellValue(linkTO.getHref());
                    	if (pageLinkCount == 1) linksRow.getCell(4).setCellStyle(firstLinkRowCellStyle);
                    	
                    	if (config.isValidateLinksOnPages()) {
                    		int httpStatusCodeInt = OutputUtil.parseInt(linkTO.getOutputHTTPStatusCode());
                    		if (httpStatusCodeInt == 0) {
                        		linksRow.createCell(5).setCellValue("");
                        		if (pageLinkCount == 1) linksRow.getCell(5).setCellStyle(firstLinkRowCellStyle);	
                    		} else {
                        		linksRow.createCell(5).setCellValue(httpStatusCodeInt);
                        		if (pageLinkCount == 1) linksRow.getCell(5).setCellStyle(firstLinkRowCellStyle);
                    		}
                    	
                    		linksRow.createCell(6).setCellValue(linkTO.getOutput());
                    		if (pageLinkCount == 1) linksRow.getCell(6).setCellStyle(firstLinkRowCellStyle);
                    	}
                    	
                    	linksRowCount ++;
                    	pageLinkCount ++;
                	}            		
            		
            	}
            }
            
    		List<SimpleOutputTO> footers = OutputUtil.getSummaryOutput(config, totalLinkCount, totalValidLinkCount, totalInvalidLinkCount,
    				totalSkippedExternalLinkCount, totalSkippedPrivateLinkCount, totalLoginRequiredLinkCount,
    				totalUnexpectedExternalRedirectLinkCount);
    			
    		summaryRowCount ++;
    		
    		XSSFRow summaryPageSubHeadingRow = summarySheet.createRow(summaryRowCount);
			
    		summaryPageSubHeadingRow.createCell(0).setCellValue("Page Summary");
    		summaryPageSubHeadingRow.getCell(0).setCellStyle(subHeadingCellStyle);
    		summaryPageSubHeadingRow.createCell(1).setCellStyle(subHeadingCellStyle); //Empty cell
    		
    		summaryRowCount ++;
    		
    		XSSFRow summaryPageCountSubHeadingRow = summarySheet.createRow(summaryRowCount);
    		
    		summaryPageCountSubHeadingRow.createCell(0).setCellValue("Page Count");
    		summaryPageCountSubHeadingRow.createCell(1).setCellValue(pageTOs.size());
    		
    		summaryRowCount ++;
    		summaryRowCount ++;
    			
    		XSSFRow summaryLinkSubHeadingRow = summarySheet.createRow(summaryRowCount);
    			
    		summaryLinkSubHeadingRow.createCell(0).setCellValue("Link Summary");
    		summaryLinkSubHeadingRow.getCell(0).setCellStyle(subHeadingCellStyle);
    		summaryLinkSubHeadingRow.createCell(1).setCellStyle(subHeadingCellStyle); //Empty cell
    			
    		summaryRowCount ++;
    			
    		for (SimpleOutputTO output: footers) {
    			XSSFRow summaryRow = summarySheet.createRow(summaryRowCount);
    			
    			summaryRow.createCell(0).setCellValue(output.getLabel());
    			
    			if (output.isStoringLong()) {
    				summaryRow.createCell(1).setCellValue(output.getLongValue());
    			} else {
    				summaryRow.createCell(1).setCellValue(output.getValue());
    			}
 
   				summaryRowCount ++;
   			}            	
	         
            for (int i = 0; i <= 1; i++) {
            	summarySheet.autoSizeColumn(i);
            }
            for (int i = 0; i <= pageHeaderColumnIndex; i++) {
            	pagesSheet.autoSizeColumn(i);
            }
            for (int i = 0; i <= 6; i++) {
            	pageLinksSheet.autoSizeColumn(i);
            }            
           
            // Add auto filter on the first 2 columns
            pageLinksSheet.setAutoFilter(new CellRangeAddress(0, linksRowCount, 0, 1));

            try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
                workbook.write(fileOut);
            }
            
            return true;
		} catch (Exception e) {		
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
	
	private XSSFCellStyle getHeadingCellStyle(XSSFWorkbook workbook, XSSFFont headingFont) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();

		cellStyle.setWrapText(true);
		cellStyle.setFont(headingFont);
		
		cellStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		return cellStyle;
	}	
	
	private XSSFCellStyle getSubHeadingCellStyle(XSSFWorkbook workbook, XSSFFont headingFont) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();

		cellStyle.setWrapText(true);
		cellStyle.setFont(headingFont);
		
		cellStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		return cellStyle;
	}	
	
	private XSSFCellStyle getFirstLinkRowCellStyle(XSSFWorkbook workbook, XSSFFont headingFont) {
		XSSFCellStyle cellStyle = workbook.createCellStyle();

		cellStyle.setFont(headingFont);
		
		return cellStyle;
	}

    private static final Log _log = LogFactoryUtil.getLog(ExcelXLSXFileOutput.class);
}