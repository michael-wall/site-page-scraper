**Introduction**
- This module can be used to export a list of Links on each Public and Private Page within a specific Liferay Site.
- This version only checks links within the Web Content Display widget, on Content Pages and Widget Pages.
- Optionally the Links can be checked to ensure that they are valid.
- The output is written to a single .txt file for example sitePageLinks_LinkTest_en_US_1748505842146.txt
- The module exposes a single custom Gogo shell command: sitePageLinkCrawler:crawlPages

**Usage**
The syntax and arguments to call Gogo shell command are as follows:

```
sitePageHTMLCrawler:crawlPages "[companyId]" "[siteId]" "[validateLinksOnPages]" "[relativeUrlPrefix]" "[publicLayoutUrlPrefix]" "[privateLayoutUrlPrefix]" "[emailAddress]" "[emailAddressEnc]" "[passwordEnc]" "[cookieDomain]" "[outputBaseFolder]"
```

For example in Liferay PaaS (with HTTPS):

```
sitePageLinkCrawler:crawlPages "23990396268826" "32920" "true" "https://webserver-lctmwsitescraper-prd.lfr.cloud" "https://webserver-lctmwsitescraper-prd.lfr.cloud/web/mw" "https://webserver-lctmwsitescraper-prd.lfr.cloud/group/mw" "test@liferay.com" "677a746b7976694c6447763272666c7658754f5167413d3d" "6b6467536d6d766b48684e63772f427451596b4e62513d3d" "webserver-lctmwsitescraper-prd.lfr.cloud" "/mnt/persistent-storage/"
```

For example in a local dev environment (with HTTP):

```
sitePageLinkCrawler:crawlPages "20096" "49006" "true" "http://localhost:8080" "http://localhost:8080/web/linktest" "http://localhost:8080/group/linktest" "test@liferay.com" "366b32764248576e783543736e55526e6e57707853773d3d" "3472704e536345712b73575a316c4a6c447a705365673d3d" "localhost" "C:/temp/crawler/"
```

Note: 
- All arguments are passed as String values with quotes and a space separator between arguments.
- In a High Availability (i.e. clustered environment) the output may only be created on the node the Gogo shell is run on depending on the outputFolder path used. The Gogo shell command can be run from the Liferay service shell to control which node is used.

Arguments:

- companyId: The companyId of the Virtual Instance that the Site resides in.
- siteId: The siteId of the Site to be crawled - get this from Site Settings > Site Configuration.
- validateLinksOnPages: Whether to validate the links on the pages, boolean so "true" or "false". Enabling this will increase the runtime of the Gogo shell command as it tries to open every URL to verify the HTTP Status Code response.
- relativeUrlPrefix: The base URL used when validating relative URLs e.g. https://mw.com
- publicLayoutUrlPrefix: The base URL used when accessing the Public Pages of the Site e.g. https://mw.com/web/intranet
- privateLayoutUrlPrefix: The base URL used when accessing the Private Pages of the Site e.g. https://mw.com/group/intranet
- emailAddress: The email address of the user to log in as. See 'Crawler User Account' section.
- emailAddressEnc: The encrypted email address of the user. See 'Crawler User Account' section.
- passwordEnc: The encrypted password of the user. See 'Crawler User Account' section.
- cookieDomain: The Cookie Domain for the credentials. See 'Crawler User Account' section.
- outputFolder: The folder that the output file should be written to. In Liferay PaaS this can be the Liferay service's persistent storage directory i.e. "/mnt/persistent-storage/".

**Crawler User Account**
The module is designed to use a non-SSO enabled account to perform the crawling. The account used should have access to all pages in the target Site.

In addition:

- The Instance Settings > User Authentication > 'Allow users to automatically log?' setting must be enabled while the tool is being setup and used. The setting can be disabled afterwards if not required. 
- The User used must be a non-SSO user and must have access to the Site and must have access to all the Pages that are to be exported.
- If necessary, create a Public page and add the 'Sign In' widget. This isn't necessary for the crawler to work but may be required to successfully login as the non-SSO user in a SSO enabled environment during setup. This page can be deleted once the encrypred credentials have been extracted. 
- To get the encrypted emailAddress and password values, perform a non-SSO login as the user in Chrome Incognito Mode with 'Remember Me' checked, then go to Dev Tools > Application > Storage > Cookies:
- The ID cookie value from above should be passed as the emailAddressEnc argument.
- The PASSWORD cookie value from above should be passed as the passwordEnc argument.
- The Domain value from the ID / PASSWORD cookies from above should be passed as the cookieDomain argument.

**Output**
- A single .txt file will be created in the file system based on the outputBaseFolder . For example /mnt/persistent-storage/sitePageLinks_LinkTest_1748434164891.txt.
- A timestamp is included to avoid overewriting an existing file for the same Site.
- On Liferay PaaS the output is written to the Liferay service and are accessible with the Liferay service shell. Depending on the outputBaseFolder the folder may not be persistent. /mnt/persistent-storage/ is persistent.

**Sample Output**
```
**********************************************************************
[1] Page Name: Public Test 1
[1] Page URL: http://localhost:8080/web/linktest/public-test-1
[1] Private Page: false
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
[2] Private Page: false
[2] Page Link Count: 0
**********************************************************************

No links found on the page.
```

**Downloading Output in Liferay PaaS**
- If /mnt/persistent-storage/ was used as the outputBaseFolder then the folder and contents can be downloaded with the LCP CLI tool.
- See here for more information on the LCP CLI tool and the download command: https://learn.liferay.com/w/liferay-cloud/reference/command-line-tool#downloading-files-from-the-liferay-service
- Ensure the latest version of the LCP CLI tool is being used.
- Download the crawler output with the following command:
```
lcp files download --prefix /siteExport/ --dest c:/temp
```

**Notes**
- This is a ‘proof of concept’ that is being provided ‘as is’ without any support coverage or warranty.
- This should be tested in a non-production environment with ‘production like’ data - i.e. volume and data types.
- The output logic can be refactored to output in spreadsheet format if required by replacing the outputToTxtFile method e.g. using https://poi.apache.org/ - HSSF for .xls and XSSF for .xlsx.
- The module has been tested in a local environment with JDK 11, Liferay DXP 7.4 U92 and OpenID SSO enabled.
- The module has mainly been tested with Public and Private Pages that were Widget Page using the Web Content Display Widget.
- The Language of the User is used when retreiving the Pages.
- A link is considered valid when it returns a 200 HTTP Status Code.
- The links on the Pages can be Absolute or Relative. Absolute links can be links to other websites but in that case they should be accessible to the server where the Gogo shell command is run from, and not require authentication.
- The Jsoup API is used to extract all links contained within the <section ... id="content"> .... </section> from Liferays themes. This is done to exclude links from header and footer etc. Change this if needed to use with a custom Theme.
- Some internal Liferay links are excluded from the output - see SitePageLinkCrawler.java, includeLink(Element link) method for more details. Update this method if needed. 
- The order of the links in the output is based on the order they are extracted from the HTML by the Jsoup API.

  **Warning**
  - Crawling a Site with for example 1,000 pages where each page has an average of 10 crawlable links will trigger 10,000 page requests in the system if validateLinksOnPages is set to true.
  - Run in non-production with a restore of recent production data first.
  - Only run in production outside of working hours to avoid impacting other users.
  - The tool is not multi-threaded so it **shouldn't** overload a system.
