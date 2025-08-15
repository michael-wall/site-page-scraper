package com.mw.site.crawler;

import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.config.InfraConfigTO;
import com.mw.site.crawler.model.LinkTO;

import java.net.URI;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

public class LayoutCrawler {
	public static final int MAX_REDIRECTS = 3;
	
    public LayoutCrawler(InfraConfigTO infraConfig, boolean isRunAsGuestUser, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, HttpServletRequest httpRequest, String cookieDomain, User user, Locale locale) {
        _infraConfig = infraConfig;
    	
    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        if (isRunAsGuestUser) {
        	_setHttpClientContextGuest(cookieDomain, locale);
        } else {
        	_setHttpClientContextCurrentUser(httpRequest, cookieDomain, locale);    	
        }
    }	

    public LayoutCrawler(InfraConfigTO infraConfig, String relativeUrlPrefix, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String idEnc, String passwordEnc, String cookieDomain, Locale locale) {
    	_infraConfig = infraConfig;
    	
    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        _setHttpClientContextFromCredentials(idEnc, passwordEnc, cookieDomain, locale);    
    }
    
    public LayoutCrawler(InfraConfigTO infraConfig, String relativeUrlPrefix, String publicLayoutUrlPrefix, String cookieDomain, User user, Locale locale) {
    	_infraConfig = infraConfig;

    	HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_infraConfig.getCrawlerUserAgent()).build();
        
        _relativeUrlPrefix = relativeUrlPrefix;
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;

        _setHttpClientContextGuest(cookieDomain, locale);
    }
    
    public String[] validateLink(String url, Locale locale, boolean skipExternalLinks) {	
    	String[] responseStringArray = {"", ""};
    	HttpEntity entity = null;
    	
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(_infraConfig.getConnectTimeout()) // time to establish connection with the server
                    .setConnectionRequestTimeout(_infraConfig.getConnectionRequestTimeout()) // time to get connection from pool
                    .setSocketTimeout(_infraConfig.getSocketTimeout()) // time waiting for data
                    .setMaxRedirects(MAX_REDIRECTS) //Default is 50, changing to 3
                    .build();
            
            URI uri = new URI(url);
            
            if (!uri.isAbsolute()) {
            	url = _relativeUrlPrefix + url;
            } else {
            	if (skipExternalLinks) {
            		if (_relativeUrlPrefixUri == null) {
            			_relativeUrlPrefixUri = new URI(_relativeUrlPrefix);
            			
            			_relativeUrlPrefixHost = _relativeUrlPrefixUri.getHost();
            		}

                	String host = uri.getHost();
                	
                	if (Validator.isNotNull(_relativeUrlPrefixHost) && Validator.isNotNull(host) && !host.equalsIgnoreCase(_relativeUrlPrefixHost)) {
                		_log.info("Skipped External Link: " + url);
                		
                        responseStringArray[0] = "" + LinkTO.SKIPPED_STATUS_CODE;
                        responseStringArray[1] = "Skipped External Link";

                        return responseStringArray;
                	}            		
            	}
            }
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, _httpClientContext);

            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();
                       
            responseStringArray[0] = "" + statusLine.getStatusCode();
            responseStringArray[1] = "" + statusLine.getReasonPhrase();

            return responseStringArray;
        } catch (Exception e) {
        	if (_log.isInfoEnabled()) {
        		_log.info("Exception validating url: " + url + ", " + e.getClass() + ": " + e.getMessage());	
        	} else if (_log.isDebugEnabled()) {
        		_log.info("Exception validating url: " + url + ", " + e.getClass() + ": " + e.getMessage(), e);		
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

    private HttpClientContext _httpClientContext;   
    
    private InfraConfigTO _infraConfig;

    private String _relativeUrlPrefix;
    private String _publicLayoutUrlPrefix;
    private String _privateLayoutUrlPrefix;
    
    private URI _relativeUrlPrefixUri;
    private String _relativeUrlPrefixHost;
 
    private HttpClient _httpClient;
    
    private static final Log _log = LogFactoryUtil.getLog(LayoutCrawler.class);    
}