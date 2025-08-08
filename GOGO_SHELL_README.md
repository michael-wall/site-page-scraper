## Site Page Crawler Custom Gogo Shell Command Setup ##
- Requires com.mw.site.crawler-1.0.0.jar module to be deployed.
- Configure the Tool: Control Panel > System Settings > Content and Data > Site Page Crawler. See **Site Page Crawler System Settings** for details.

## Site Page Crawler Custom Gogo Shell Command Usage ##
- The syntax and arguments to call the custom Gogo shell command are as follows:

```
sitePageHTMLCrawler:crawlPages "[companyId]" "[siteId]" "[relativeUrlPrefix]" "[publicLayoutUrlPrefix]" "[privateLayoutUrlPrefix]" "[emailAddress]" "[loginIdEnc]" "[passwordEnc]" "[cookieDomain]"
```

- For example in Liferay PaaS (with HTTPS):

```
sitePageLinkCrawler:crawlPages "23990396268826" "32920" "https://webserver-lctmwsitescraper-prd.lfr.cloud" "https://webserver-lctmwsitescraper-prd.lfr.cloud/web/mw" "https://webserver-lctmwsitescraper-prd.lfr.cloud/group/mw" "test@liferay.com" "677a746b7976694c6447763272666c7658754f5167413d3d" "6b6467536d6d766b48684e63772f427451596b4e62513d3d" "webserver-lctmwsitescraper-prd.lfr.cloud"
```

- For example in a local dev environment (with HTTP):

```
sitePageLinkCrawler:crawlPages "20096" "49006" "http://localhost:8080" "http://localhost:8080/web/linktest" "http://localhost:8080/group/linktest" "test@liferay.com" "366b32764248576e783543736e55526e6e57707853773d3d" "3472704e536345712b73575a316c4a6c447a705365673d3d" "localhost"
```

Note:
- All arguments are passed as String values with quotes and a space separator between arguments.
- In a High Availability (i.e. clustered environment) the output may only be created on the node the Gogo Shell Command is run on depending on the outputFolder path used. The Gogo Shell Command can be run from the Liferay service shell to control which node is used.

Arguments:
- **companyId**: The companyId of the Virtual Instance that the Site resides in.
- **siteId**: The siteId of the Site to be crawled - get this from Site Settings > Site Configuration.
- **relativeUrlPrefix**: The base URL used when validating relative URLs e.g. https://mw.com
- **publicLayoutUrlPrefix**: The base URL used when accessing the Public Pages of the Site e.g. https://mw.com/web/intranet
- **privateLayoutUrlPrefix**: The base URL used when accessing the Private Pages of the Site e.g. https://mw.com/group/intranet
- **emailAddress**: The email address of the user to log in as. See 'Crawler User Account' section.
- **loginIdEnc**: The encrypted login ID (email address or screenName) of the user. See 'Crawler User Account' section.
- **passwordEnc**: The encrypted password of the user. See 'Crawler User Account' section.
- **cookieDomain**: The Cookie Domain for the credentials. See 'Crawler User Account' section.

## Site Page Crawler Custom Gogo Shell Command > Crawler User Account ##
- The custom Gogo Shell Command MUST use a non-SSO enabled account to perform the crawling. The account used must have access to all pages in the target Site.
- The Instance Settings > User Authentication > 'Allow users to automatically log?' setting must be enabled while the tool is being setup and used. The setting can be disabled afterwards if not required. 
- If necessary, create a Public page and add the 'Sign In' widget. This isn't necessary for the crawler to work but may be required to successfully login as the non-SSO user in a SSO enabled environment during setup. This page can be deleted once the encrypred credentials have been extracted. 
- To get the encrypted loginId and password values, perform a non-SSO login as the user in Chrome Incognito Mode with 'Remember Me' checked, then go to Dev Tools > Application > Storage > Cookies:
  - The ID cookie value from above should be passed as the loginIdEnc argument.
  - The PASSWORD cookie value from above should be passed as the passwordEnc argument.
- The Domain value from the ID / PASSWORD cookies from above should be passed as the cookieDomain argument.

## Site Page Crawler Custom Gogo Shell Command > Output ##
- A single .txt file will be created in the file system based on the outputBaseFolder . For example /mnt/persistent-storage/sitePageLinks_LinkTest_1748434164891.txt.
- A timestamp is included to avoid overewriting an existing file for the same Site.
- On Liferay PaaS the output is written to the Liferay service and are accessible with the Liferay service shell. Depending on the outputBaseFolder the folder may not be persistent. /mnt/persistent-storage/ is persistent.

## Site Page Crawler Custom Gogo Shell Command > Downloading Output in Liferay PaaS ##
- If /mnt/persistent-storage/ was used as the outputBaseFolder then the folder and contents can be downloaded with the LCP CLI tool.
- See here for more information on the LCP CLI tool and the download command: https://learn.liferay.com/w/liferay-cloud/reference/command-line-tool#downloading-files-from-the-liferay-service
- Ensure the latest version of the LCP CLI tool is being used.
- Download the crawler output with the following command:
```
lcp files download --prefix /siteExport/ --dest c:/temp
```
