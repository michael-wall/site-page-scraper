## Introduction ##
- This tool can be used to export a list of Links on each Public and Private Page within a specific Liferay Site.
  - Optionally each Pages Guest Role View Permission can be checked..
  - Optionally each Pages Links can be checked to ensure that they are valid.

## Usage ##
- The tool can be run 2 ways:
  - The preferred approach is to use the Site Page Crawler Widget which uploads the output report to a custom Liferay Objects entity to be easily downloaded from within Liferay. Setup is required before the Widget can be used. See 'Site Page Crawler Widget Setup' section. Using this approach, the logged in user can be a SSO user or a non-SSO user.
  - The alternative is the custom Gogo Shell Command which saves the output report on the Liferay server to be downloaded from the Liferay server. Using this approach, the account used MUST be a non-SSO account. See [here](GOGO_SHELL_README.md) for using the custom Gogo Shell Command. The **Site Page Crawler System Settings** should be setup first and the **General Notes** and **Warnings** apply to this approach also.

##  Site Page Crawler System Settings ##
- Required for both the Site Page Crawler Widget and for the Custom Gogo Shell Command.
  - In the case of the Site Page Crawler Widget the boolean values are set as the defaults in the GUI and the user can choose to override any of the values before clicking 'Run Site Page Crawler'.
  - In the case of the Custom Gogo Shell Command the values from the System Settings are applied.
- Configure the Tool: Control Panel > System Settings > Content and Data > Site Page Crawler
  - **Output Folder**: The folder that the output file should be written to. In Liferay PaaS this can be the Liferay service's persistent storage directory i.e. "/mnt/persistent-storage/".
  - **Object Definition ERC**: The ERC of the Liferay Objects Definition where the Site Page Crawler output is saved. Default is CRAWLER_OUTPUT. Used by the Site Page Crawler Widget only.
  - **Page Body Selector**: CSS Selector to identify the Page Body. Default is section#content. This is used to exclude the page header and page footer when looking for links on a page.
  - **Web Content Display Widget Links Only**: Whether to include Links from Web Content Display Widget occurrences only. Default is true.
  - **Include Public Pages**: Whether to include public pages. Default is true.
  - **Include Private Pages**: Whether to include private pages. Default is true.
  - **Include Hidden Pages**: Whether to include hidden pages. Default is false. This is the Page Settings > General > Basic Info > Hidden from Menu Display field.
  - **Check Page Guest Role View Permission**: Check whether the Guest Role has View permission for the page. Default is false.
  - **Validate Links On Pages**: Whether to validate links on pages. Default is false. Enabling this will increase the runtime of the Gogo shell command as it tries to open every URL to verify the HTTP Status Code response.

## Site Page Crawler Widget Setup ##
- Requires com.mw.site.crawler-1.0.0.jar AND com.mw.site.crawler.web-1.0.0.jar modules to be deployed.
- Configure the Tool: Control Panel > System Settings > Content and Data > Site Page Crawler. See **Site Page Crawler System Settings** for details.
- Create a Site Scoped Liferay Object called 'Site Page Crawler Output' with ERC set to CRAWLER_OUTPUT and set the Panel Link to Site Administration > Publishing.
- Add the following fields to the custom Liferay Object:
  - **requestor**: Type Text, Mandatory, with the additional settings: Searchable: False
  - **site**: Type Text, Mandatory, with the additional settings: Searchable: False
  - **locale**: Type Text, Mandatory, with the additional settings: Searchable: False
  - **crawledPages**: Type Long Integer, Mandatory, with the additional settings: Searchable: False
  - **created**: Type Date and Time, Mandatory, Time Storage: Convert to UTC, with the additional settings: Searchable: False
  - **output**: Type Attachment, Mandatory, Request Files: Upload Directly from the User's Computer, Show Files in Documents and Media OFF, with the additional settings: Accepted File Extensions: txt, Searchable: False
 
Setup Notes:
- Ensure the users running the report have access to the Object Entry list screen and to the Liferay Object Entry records themselves.
- Update the code in CrawlerPortletActionCommand if restricting access to the specific records is necessary.
- Exclude the 'Site Page Crawler Output' Object from  Search Results e.g. with the Type Facet etc.

## Site Page Crawler Widget Usage ##
- Within the Site to be crawled, go to Site > Site Menu > Site Builder > Site Page Crawler.
- Override the default values if required
  - For example if the default for 'Include Hidden Pages' is false and you wish to include hidden pages for this run then enable the corresponding checkbox.
- Click 'Run Site Page Crawler'
  - It first checks if pages exist and returns a message e.g. The Asynchronous Site Page Crawler has been triggered at 11:19:29 UTC. You will receive a notification when it completes.
  - It runs asynchronously and when it completes it will send a Liferay notifcation to confirm it completed e.g. The Asynchronous Site Page Crawler has completed successfully for Site Guest. The output was saved to Site Page Crawler Outputs with ID 53421.
  - Go to Site Administration > Publishing > Site Page Crawler Outputs and find the record where the ID matches.
  - The output file can be downloaded from the link in the 'output' column.
- **Note that the widget runs in the context of the current users session, so the user must remain logged in for the asynchronous Site Page Crawler process to be able to crawl pages that don't have Guest role View access.**

## Site Page Crawler Widget Notes ##
- Running the Site Page Crawler from the Site Page Crawler Widget triggers the Site Page Crawler asynchronously.
- The **Site Page Crawler Widget** requires less configuration settings and arguments than the **Custom Gogo Shell Command** since it is triggered from within a Liferay DXP Site by an authenticated user. This means:
  - It can infer the companyId and groupId from the current Site
  - It can infer the hostname, port and protocol from the current request. This hostname is used as the cookieDomain and the hostname, port and protocol are used for generating public and private friendly URLs.
  - It infers the friendly URL syntax
  - It can copy the cookies from the current user and pass these to the crawler to make server side authenticated requests to retrieve the pages as the user.
- **Note that the widget runs in the context of the current users session, so the user must remain logged in for the asynchronous Site Page Crawler process to be able to crawl pages that don't have Guest role View access.**

## Sample Output ## 
```
Site Name: LinkTest
Locale: en_US
Crawl Public Pages: Yes
Crawl Private Pages: Yes
Crawl Hidden Pages: Yes
Check Page Guest Role View Permission: Yes
Web Content Display Widget Links Only: Yes
Validate Links On Pages: Yes

Page Count: 14

**********************************************************************
[1] Page Name: Public Test 1
[1] Page URL: http://localhost:8080/web/linktest/public-test-1
[1] Private Page: No
[1] Page Guest View Permission Enabled: Yes
[1] Page Link Count: 4
[1] Valid Link Count: 3
[1] Invalid Link Count: 1
**********************************************************************

Link Label: Test 1
Link URL: http://localhost:8080/group/linktest/test-3
Link appears to be valid.

Link Label: Test 3
Link URL: /group/linktest/test-3
Link appears to be valid.

Link Label: Energy and Utilities
Link URL: https://www.liferay.com/industries/energy-and-utilities
Link appears to be valid.

Link Label: Test 4 (Missing)
Link URL: http://localhost:8080/group/linktest/test-4
Link not verified: 404

**********************************************************************
[2] Page Name: Public Test 2
[2] Page URL: http://localhost:8080/web/linktest/public-test-2
[2] Private Page: No
[1] Page Guest View Permission Enabled: No
[2] Page Link Count: 0
**********************************************************************

No links found on the page.
```

## General Notes ##
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- This should be tested in a non-production environment with ‘production like’ data - i.e. volume and data types.
- The output logic can be refactored to output in spreadsheet format if required by replacing the outputToTxtFile method e.g. using https://poi.apache.org/ - HSSF for .xls and XSSF for .xlsx.
- The modules have been tested in a local environment with JDK 11, Liferay DXP 7.4 U92 and SAML SSO enabled.
- The modules have been tested with Public and Private Pages that were either Content Pages or Widget Page using the Web Content Display Widget.
- The Language of the User is used when retreiving the Pages.
- A link is considered valid when it returns a 200 HTTP Status Code.
- The links on the Pages can be Absolute or Relative. Absolute links can be links to other websites but in that case they should be accessible to the server where the Gogo shell command is run from, and not require authentication.
- The Jsoup API is used to extract all links contained within the <section ... id="content"> .... </section> from Liferays themes. This is done to exclude links from header and footer etc. Change this if needed in Control Panel > System Settings > Content and Data > Site Page Crawler.
- Some internal Liferay links are excluded from the output - see SitePageLinkCrawler.java, includeLink(Element link) method for more details. Update this method if needed.
  - Anchor links to the current page, mailto:,  file://, and tel: links are ignored for example.
- The order of the links in the output is based on the order they are extracted from the HTML by the Jsoup API.
- Bear in mind that Content Pages may have different Experiences or Widget and Content Pages render different content based on the user accessing the page e.g. based on Widget Permissions etc.

## Warning ##
- Crawling a Site with for example 1,000 pages where each page has an average of 10 crawlable links will trigger 10,000 page requests in the system if validateLinksOnPages is set to true.
- Run in non-production with a restore of recent production data first.
- Ideally run in production outside of working hours to avoid impacting other users.
- The tool is not multi-threaded so it **shouldn't** overload a system, but that means it takes longer to run.
