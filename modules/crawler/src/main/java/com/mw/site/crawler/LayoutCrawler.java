package com.mw.site.crawler;

import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.VirtualHost;
import com.liferay.portal.kernel.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.config.InfraConfigTO;
import com.mw.site.crawler.model.LinkTO;
import com.mw.site.crawler.model.VirtualHostTO;
import com.mw.site.crawler.util.CrawlerUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

public class LayoutCrawler {
	public static final int MAX_REDIRECTS = 5;
	
    /**
     * Used by web
     */
    public LayoutCrawler(long companyId, long siteGroupId, InfraConfigTO infraConfig, boolean isRunAsGuestUser, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, HttpServletRequest httpRequest, String cookieDomain, User user, Locale locale) {
        _infraConfig = infraConfig;
    	
    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    	
        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
		try {
			_relativeUrlPrefixUri = new URI(_relativeUrlPrefix);
		} catch (URISyntaxException e) {
			_log.error("Exception parsing URI from _relativeUrlPrefix for " + _relativeUrlPrefix);
		}
		_host = _relativeUrlPrefixUri.getHost();
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        _companyId = companyId;
        _siteGroupId = siteGroupId;
        
        _defaultLanguageId = CrawlerUtil.getSiteDefaultLocale(siteGroupId).toString();
        
        _allVirtualHosts = getAllVirtualHosts();
        _siteVirtualHosts = getSiteVirtualHosts(_siteGroupId, _defaultLanguageId);
        
        _asynchronous = true;
        
        if (isRunAsGuestUser) {
        	_setHttpClientContextGuest(cookieDomain, locale);
        } else {
        	_setHttpClientContextCurrentUser(httpRequest, cookieDomain, locale);    	
        }
    }	

    /**
     * Used by gogo shell sitePageHTMLCrawler:crawlPagesAsUser
     */    
    public LayoutCrawler(long companyId, long siteGroupId, InfraConfigTO infraConfig, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String idEnc, String passwordEnc, String cookieDomain, Locale locale) {
    	_infraConfig = infraConfig;
    	
    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
		try {
			_relativeUrlPrefixUri = new URI(_relativeUrlPrefix);
		} catch (URISyntaxException e) {
			_log.error("Exception parsing URI from _relativeUrlPrefix for " + _relativeUrlPrefix);
		}
		_host = _relativeUrlPrefixUri.getHost();
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        _companyId = companyId;
        _siteGroupId = siteGroupId;
        _defaultLanguageId = CrawlerUtil.getSiteDefaultLocale(siteGroupId).toString();
        
        _allVirtualHosts = getAllVirtualHosts();
        _siteVirtualHosts = getSiteVirtualHosts(_siteGroupId, _defaultLanguageId);
        
        _asynchronous = false;
        
        _setHttpClientContextFromCredentials(idEnc, passwordEnc, cookieDomain, locale);    
    }
    
    /**
     * Used by gogo shell sitePageHTMLCrawler:crawlPagesAsGuest
     */   
    public LayoutCrawler(long companyId, long siteGroupId, InfraConfigTO infraConfig, String relativeUrlPrefix, String publicLayoutUrlPrefix, String cookieDomain, User user, Locale locale) {
    	_infraConfig = infraConfig;

    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
		try {
			_relativeUrlPrefixUri = new URI(_relativeUrlPrefix);
		} catch (URISyntaxException e) {
			_log.error("Exception parsing URI from _relativeUrlPrefix for " + _relativeUrlPrefix);
		}
		_host = _relativeUrlPrefixUri.getHost();
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        
        _companyId = companyId;
        _siteGroupId = siteGroupId;
        _defaultLanguageId = CrawlerUtil.getSiteDefaultLocale(siteGroupId).toString();
        
        _allVirtualHosts = getAllVirtualHosts();
        _siteVirtualHosts = getSiteVirtualHosts(_siteGroupId, _defaultLanguageId);
        
        _asynchronous = false;

        _setHttpClientContextGuest(cookieDomain, locale);
    }
    
    public String[] validateLink(String url, Locale locale, boolean skipExternalLinks, boolean isRunAsGuest) {	
    	String[] responseStringArray = {"", ""};
    	HttpEntity entity = null;
    	
    	String privatePagePrefix = PortalUtil.getPathFriendlyURLPrivateGroup() + "/";
    	
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(_infraConfig.getConnectTimeout()) // time to establish connection with the server
                    .setConnectionRequestTimeout(_infraConfig.getConnectionRequestTimeout()) // time to get connection from pool
                    .setSocketTimeout(_infraConfig.getSocketTimeout()) // time waiting for data
                    .setMaxRedirects(MAX_REDIRECTS) //Default is 50, changing to 3
                    .build();
            
            URI uri = new URI(url);
            String host = "";
            
            boolean skipAsPrivatePage = false;
            boolean skipAsExternal = false;
            
            boolean isExternal = false;

            if (!uri.isAbsolute()) { //Relative
            	url = _relativeUrlPrefix + url;
            	
            	if (isRunAsGuest) {
            		if (uri.toString().startsWith(privatePagePrefix)) {
            			skipAsPrivatePage = true;
            		}
            	}
            } else { // Absolute
            	host = uri.getHost();
            	
            	isExternal = isExternalURL(host, _host);
            	
            	if (isRunAsGuest) {
            		if (uri.getPath().startsWith(privatePagePrefix)) {
            			skipAsPrivatePage = true;
            		}
            	}
            	
            	if (skipExternalLinks && isExternal) {
            		skipAsExternal = true;
            	}
            }
            
            if (isRunAsGuest && skipAsPrivatePage) {
        		_log.info("Skipped Private Page Link: " + url);
        		
                responseStringArray[0] = "" + LinkTO.SKIPPED_PRIVATE_PAGE_STATUS_CODE;
                responseStringArray[1] = "Skipped Private Page Link.";

                return responseStringArray;            	
            }
            
            if (skipExternalLinks && skipAsExternal) {
        		_log.info("Skipped Other Hostname Link: " + url);
        		
                responseStringArray[0] = "" + LinkTO.SKIPPED_EXTERNAL_LINK_STATUS_CODE;
                responseStringArray[1] = "Skipped Other Hostname Link to " + getHostSummary(host);

                return responseStringArray;
            }
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, _httpClientContext);

            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();
            
            HttpUriRequest finalReq = (HttpUriRequest) _httpClientContext.getRequest();
            HttpHost finalHost = _httpClientContext.getTargetHost();
            String finalUrl = finalReq.getURI().isAbsolute() 
                    ? finalReq.getURI().toString()
                    : (finalHost.toURI() + finalReq.getURI());
            
            String finalPath = finalReq.getURI().getPath();
            String finalQuery = finalReq.getURI().getQuery();
            
            // End URL is a login screen (non-SSO or subset of SSO e.g. if multiple IdP selector shown)
            if (Validator.isNotNull(finalPath) && finalPath.startsWith("/c/portal/login")) {
        		_log.info("Login Redirect Triggered: " + url);
        		
                responseStringArray[0] = "" + LinkTO.LOGIN_REDIRECT_STATUS_CODE;
                responseStringArray[1] = "Login Redirect Triggered.";

                return responseStringArray;       
            }
            
            // End URL is a login screen (non-SSO or subset of SSO e.g. if multiple IdP selector shown)
            if (Validator.isNotNull(finalQuery) && finalQuery.indexOf("_com_liferay_login_web_portlet_LoginPortlet") > 0) {
        		_log.info("Login Redirect Triggered: " + url);
        		
                responseStringArray[0] = "" + LinkTO.LOGIN_REDIRECT_STATUS_CODE;
                responseStringArray[1] = "Login Redirect Triggered.";

                return responseStringArray;       
            }
            
            URI finalUrlUri = new URI(finalUrl);
            
            boolean finalUrlIsExternal = isExternalURL(finalUrlUri.getHost(), _host);
            
            // Started internal, ended external... possibly due to SSO but can't be confirmed...
            if (!isExternal && finalUrlIsExternal) {
        		_log.info("Unexpected Redirect to other hostname triggered: " + url);
        		
                responseStringArray[0] = "" + LinkTO.UNEXPECTED_EXTERNAL_REDIRECT_STATUS_CODE;
                responseStringArray[1] = "Unexpected Redirect to other hostname triggered to " + getHostSummary(finalUrlUri.getHost());

                return responseStringArray;       
            }
            
            responseStringArray[0] = "" + statusLine.getStatusCode();
            responseStringArray[1] = "" + statusLine.getReasonPhrase();

            return responseStringArray;
        } catch (Exception e) {
        	if (_log.isDebugEnabled()) {
        		_log.info("Exception validating url: " + url + ", " + e.getClass() + ": " + e.getMessage(), e);
        	} else if (_log.isInfoEnabled()) {
        		_log.info("Exception validating url: " + url + ", " + e.getClass() + ": " + e.getMessage());	
        	}
                	
        	responseStringArray[0] = "" + LinkTO.EXCEPTION_STATUS_CODE;
        	responseStringArray[1] = e.getMessage();
        } finally {
        	try {
        		if (entity != null) EntityUtils.consume(entity);
        	} catch (Exception e) {}
        }
        
        return responseStringArray;
    }
    
    private boolean isExternalURL(String host, String relativeUrlPrefixHost) {
    	
    	if (Validator.isNotNull(relativeUrlPrefixHost) && Validator.isNotNull(host) && !host.equalsIgnoreCase(relativeUrlPrefixHost)) {
    		return true;
    	}
    	
    	return false;
    }

    public String[] getLayoutContent(Layout layout, Locale locale) {
    	String[] responseStringArray = {"", ""};
    	String layoutFullURL = "";
    	HttpEntity entity = null;

        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(_infraConfig.getConnectTimeout()) // time to establish connection with the server
                    .setConnectionRequestTimeout(_infraConfig.getConnectionRequestTimeout()) // time to get connection from pool
                    .setSocketTimeout(_infraConfig.getSocketTimeout()) // time waiting for data
                    .setMaxRedirects(MAX_REDIRECTS) //Default is 50, changing to 3
                    .build();        	
        	
            if (layout.isPrivateLayout()) {
            	layoutFullURL = _privateLayoutUrlPrefix + layout.getFriendlyURL(locale);
            } else {
            	layoutFullURL = _publicLayoutUrlPrefix + layout.getFriendlyURL(locale);
            }

            HttpGet httpGet = new HttpGet(layoutFullURL);
            httpGet.setConfig(requestConfig);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, _httpClientContext);
            
            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            	responseStringArray[0] = layoutFullURL;
            	responseStringArray[1] = EntityUtils.toString(entity);
            	
                return responseStringArray;
            } 
        }
        catch (Exception e) {
         	if (_log.isInfoEnabled()) {
         		_log.info("Unable to crawl layout: " + layoutFullURL + ", " + e.getClass() + ": " + e.getMessage());
        	} else if (_log.isDebugEnabled()) {
        		_log.info("Unable to crawl layout: " + layoutFullURL + ", " + e.getClass() + ": " + e.getMessage(), e);
        	}        	 
        } finally {
        	try {
        		if (entity != null) EntityUtils.consume(entity);
        	} catch (Exception e) {}
        }

        return null;
    }

    private void _setHttpClientContextFromCredentials(String idEnc, String passwordEnc, String cookieDomain, Locale locale) {

    	if (_httpClientContext != null) return;

        CookieStore cookieStore = new BasicCookieStore();
       
        BasicClientCookie autoIdClientCookie =
                _createClientCookie(CookiesConstants.NAME_ID, idEnc, cookieDomain);
        BasicClientCookie autoPasswordClientCookie =
                _createClientCookie(CookiesConstants.NAME_PASSWORD, passwordEnc, cookieDomain);
        BasicClientCookie rememberMeClientCookie =
                _createClientCookie(CookiesConstants.NAME_REMEMBER_ME, "true", cookieDomain);

        BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(CookiesConstants.NAME_GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale), cookieDomain);
        
        cookieStore.addCookie(autoIdClientCookie);
        cookieStore.addCookie(autoPasswordClientCookie);
        cookieStore.addCookie(rememberMeClientCookie);
        cookieStore.addCookie(guestLanguageIdClientCookie);

        _httpClientContext = new HttpClientContext();

        _httpClientContext.setCookieStore(cookieStore);
    }
    
    private void _setHttpClientContextCurrentUser(HttpServletRequest httpRequest, String cookieDomain, Locale locale) {

    	if (_httpClientContext != null) return;

        CookieStore cookieStore = new BasicCookieStore();
        
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
        	_log.info("Original Cookies count: " + cookies.length);
        	
        	String requiredCookies[] = {
        		CookiesConstants.NAME_COMPANY_ID,
        		CookiesConstants.NAME_ID,
        		CookiesConstants.NAME_JSESSIONID,
        		CookiesConstants.NAME_COOKIE_SUPPORT,
        		CookiesConstants.NAME_GUEST_LANGUAGE_ID
        	};
        	
            for (Cookie servletCookie : cookies) {
            	// This MAY break SSO, so commenting out...
            	//if (Arrays.asList(requiredCookies).contains(servletCookie.getName())) {            	
	                BasicClientCookie clientCookie = new BasicClientCookie(servletCookie.getName(), servletCookie.getValue());
	                clientCookie.setDomain(cookieDomain);
	                clientCookie.setPath(servletCookie.getPath() != null ? servletCookie.getPath() : "/");
	                cookieStore.addCookie(clientCookie);
            	//}
            }
            
            _log.info("Copied Cookies count: " + cookieStore.getCookies().size());
        }

        _httpClientContext = new HttpClientContext();

        _httpClientContext.setCookieStore(cookieStore);
    }    
    
    private void _setHttpClientContextGuest(String cookieDomain, Locale locale) {

        if (_httpClientContext != null) return;

        CookieStore cookieStore = new BasicCookieStore();
        
        BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(CookiesConstants.NAME_GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale), cookieDomain);
        
        cookieStore.addCookie(guestLanguageIdClientCookie);

        _httpClientContext = new HttpClientContext();

        _httpClientContext.setCookieStore(cookieStore);
    }       

    private BasicClientCookie _createClientCookie(
            String cookieName, String cookieValue, String domain) {

        BasicClientCookie basicClientCookie =
                new BasicClientCookie(cookieName, cookieValue);

        basicClientCookie.setDomain(domain);

        return basicClientCookie;
    }
    
    private String getHostSummary(String host) {
    	if (Validator.isNull(host)) return host;
        
        if (host.equalsIgnoreCase("localhost") && !_host.equalsIgnoreCase("localhost")) {
        	return host + " (Avoid using localhost in Links...)";
        }
    	
        for (VirtualHostTO vh: _siteVirtualHosts) {
			if (vh.getHostName().equalsIgnoreCase(host)) {
				if (vh.isPublicVirtualHost()) {
					return host + " (Public Page Virtual Host for this Site for locale " + vh.getLanguageId() + ")";
				} else {
					return host + " (Private Page Virtual Host for this Site for locale " + vh.getLanguageId() + ")";
				}
			}
		}
        
        //Fallback to wider scope...
        for (VirtualHost vh: _allVirtualHosts) {
			if (vh.getHostname().equalsIgnoreCase(host) && vh.getCompanyId() == _companyId) {
				return host + " (Virtual Host in this Virtual Instance.)";
			} else if (vh.getHostname().equalsIgnoreCase(host)) {
				return host + " (Virtual Host in other Virtual Instance.)";
			}
		}
    	
        return host;
    }
    
    private List<VirtualHost> getAllVirtualHosts() {
    	
    	//ALL not just current company
    	List<VirtualHost> virtualHosts = VirtualHostLocalServiceUtil.getVirtualHosts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
    	
    	return virtualHosts;
    }
    
    private List<VirtualHostTO> getSiteVirtualHosts(long siteGroupId, String defaultLanguageId) {
    	
    	//Just this Site
    	List<VirtualHostTO> virtualHosts = CrawlerUtil.getSiteVirtualHosts(siteGroupId, defaultLanguageId);
    	
    	return virtualHosts;
    }
    
    public boolean isAsynchronous() {
    	return _asynchronous;
    }
    
    public String getRelativeUrlPrefix() {
    	return _relativeUrlPrefix;
    }
    
    private boolean _asynchronous = false;
    
    private List<VirtualHost> _allVirtualHosts;
    private List<VirtualHostTO> _siteVirtualHosts;

    private HttpClientContext _httpClientContext;   
    
    private InfraConfigTO _infraConfig;

    private String _relativeUrlPrefix;
    private String _publicLayoutUrlPrefix;
    private String _privateLayoutUrlPrefix;
    
    private URI _relativeUrlPrefixUri;
    private String _host;
 
    private HttpClient _httpClient;
    
    private long _companyId;
    private long _siteGroupId;
    
    private String _defaultLanguageId;
    
    private static final Log _log = LogFactoryUtil.getLog(LayoutCrawler.class);    
}