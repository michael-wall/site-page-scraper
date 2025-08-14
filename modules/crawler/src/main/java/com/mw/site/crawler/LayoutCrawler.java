package com.mw.site.crawler;

import com.liferay.portal.kernel.cookies.constants.CookiesConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.site.crawler.config.ConfigTO;
import com.mw.site.crawler.config.SitePageCrawlerConfiguration;

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
	
    public LayoutCrawler(ConfigTO config, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, HttpServletRequest httpRequest, String cookieDomain, User user, Locale locale) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(getCrawlerUserAgent(config.getCrawlerUserAgent())).build();
        
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        if (config.isRunAsGuestUser()) {
        	httpClientContext = _getHttpClientContextGuest(cookieDomain, locale);
        } else {
        	httpClientContext = _getHttpClientContextCurrentUser(httpRequest, cookieDomain, locale);    	
        }
    }	

    public LayoutCrawler(ConfigTO config, String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String idEnc, String passwordEnc, String cookieDomain, Locale locale) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(getCrawlerUserAgent(config.getCrawlerUserAgent())).build();
        
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        httpClientContext =  _getHttpClientContextFromCredentials(idEnc, passwordEnc, cookieDomain, locale);    
    }
    
    public LayoutCrawler(ConfigTO config, String publicLayoutUrlPrefix, String cookieDomain, User user, Locale locale) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(getCrawlerUserAgent(config.getCrawlerUserAgent())).build();
        
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;

        httpClientContext = _getHttpClientContextGuest(cookieDomain, locale);
    }
    
    public String[] validateLink(ConfigTO config, String url, String relativeUrlPrefix, Locale locale) {	
    	String[] responseStringArray = {"", ""};
    	HttpEntity entity = null;
    	
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(getTimeout(config.getConnectTimeout())) // time to establish connection with the server
                    .setConnectionRequestTimeout(getTimeout(config.getConnectionRequestTimeout())) // time to get connection from pool
                    .setSocketTimeout(getTimeout(config.getSocketTimeout())) // time waiting for data
                    .build();
            
            URI uri = new URI(url);
            
            if (!uri.isAbsolute()) {
            	url = relativeUrlPrefix + url;
            }
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, httpClientContext);

            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();
                       
            responseStringArray[0] = "" + statusLine.getStatusCode();
            responseStringArray[1] = "" + statusLine.getReasonPhrase();

            return responseStringArray;
        } catch (Exception e) {
        	_log.info("Exception validating url: " + url + ", " + e.getClass() + ": " + e.getMessage(), e);
        	
        	responseStringArray[0] = "-1";
        	responseStringArray[1] = e.getMessage();
        } finally {
        	try {
        		if (entity != null) EntityUtils.consume(entity);
        	} catch (Exception e) {}
        }
        
        return responseStringArray;
    }

    public String[] getLayoutContent(ConfigTO config, Layout layout, Locale locale) {
    	String[] responseStringArray = {"", ""};
    	String layoutFullURL = "";
    	HttpEntity entity = null;

        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(getTimeout(config.getConnectTimeout())) // time to establish connection with the server
                    .setConnectionRequestTimeout(getTimeout(config.getConnectionRequestTimeout())) // time to get connection from pool
                    .setSocketTimeout(getTimeout(config.getSocketTimeout())) // time waiting for data
                    .build();        	
        	
            if (layout.isPrivateLayout()) {
            	layoutFullURL = _privateLayoutUrlPrefix + layout.getFriendlyURL(locale);
            } else {
            	layoutFullURL = _publicLayoutUrlPrefix + layout.getFriendlyURL(locale);
            }

            HttpGet httpGet = new HttpGet(layoutFullURL);
            httpGet.setConfig(requestConfig);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, httpClientContext);
            
            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            	responseStringArray[0] = layoutFullURL;
            	responseStringArray[1] = EntityUtils.toString(entity);
            	
                return responseStringArray;
            } 
        }
        catch (Exception e) {
        	 _log.info("Unable to crawl layout: " + layoutFullURL + ", " + e.getClass() + ": " + e.getMessage(), e);
        } finally {
        	try {
        		if (entity != null) EntityUtils.consume(entity);
        	} catch (Exception e) {}
        }

        return null;
    }

    private HttpClientContext _getHttpClientContextFromCredentials(String idEnc, String passwordEnc, String cookieDomain, Locale locale) {

        if (_httpClientContext != null) {
            return _httpClientContext;
        }

        CookieStore cookieStore = new BasicCookieStore();
       
        BasicClientCookie autoIdClientCookie =
                _createClientCookie(CookiesConstants.NAME_ID, idEnc, cookieDomain);
        BasicClientCookie autoPasswordClientCookie =
                _createClientCookie(CookiesConstants.NAME_PASSWORD, passwordEnc, cookieDomain);
        BasicClientCookie rememberMeClientCookie =
                _createClientCookie(CookiesConstants.NAME_REMEMBER_ME, _rememberMe, cookieDomain);

        BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(CookiesConstants.NAME_GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale), cookieDomain);
        
        cookieStore.addCookie(autoIdClientCookie);
        cookieStore.addCookie(autoPasswordClientCookie);
        cookieStore.addCookie(rememberMeClientCookie);
        cookieStore.addCookie(guestLanguageIdClientCookie);

        HttpClientContext httpClientContext = new HttpClientContext();

        httpClientContext.setCookieStore(cookieStore);

        return httpClientContext;
    }
    
    private HttpClientContext _getHttpClientContextCurrentUser(HttpServletRequest httpRequest, String cookieDomain, Locale locale) {

        if (_httpClientContext != null) {
            return _httpClientContext;
        }

        CookieStore cookieStore = new BasicCookieStore();
        
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
        	_log.info("Cookies count: " + cookies.length);
        	
        	//TODO MW See if we can filter to only copying the cookies we care about...
            for (Cookie servletCookie : cookies) {
                BasicClientCookie clientCookie = new BasicClientCookie(servletCookie.getName(), servletCookie.getValue());
                clientCookie.setDomain(cookieDomain);
                clientCookie.setPath(servletCookie.getPath() != null ? servletCookie.getPath() : "/");
                cookieStore.addCookie(clientCookie);
            }
        }

        HttpClientContext httpClientContext = new HttpClientContext();

        httpClientContext.setCookieStore(cookieStore);

        return httpClientContext;
    }    
    
    private HttpClientContext _getHttpClientContextGuest(String cookieDomain, Locale locale) {

        if (_httpClientContext != null) {
            return _httpClientContext;
        }

        CookieStore cookieStore = new BasicCookieStore();
        
        BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(CookiesConstants.NAME_GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale), cookieDomain);
        
        cookieStore.addCookie(guestLanguageIdClientCookie);

        HttpClientContext httpClientContext = new HttpClientContext();

        httpClientContext.setCookieStore(cookieStore);

        return httpClientContext;
    }       

    private BasicClientCookie _createClientCookie(
            String cookieName, String cookieValue, String domain) {

        BasicClientCookie basicClientCookie =
                new BasicClientCookie(cookieName, cookieValue);

        basicClientCookie.setDomain(domain);

        return basicClientCookie;
    }
    
    private int getTimeout(int value) {
    	if (value <=0) return SitePageCrawlerConfiguration.DEFAULT_TIMEOUT;
    	
    	return value;
    }

    private String getCrawlerUserAgent(String configCrawlerUserAgent) {
    	if (Validator.isNull(configCrawlerUserAgent)) return SitePageCrawlerConfiguration.DEFAULT_CRAWLER_USER_AGENT;
    	
    	return configCrawlerUserAgent;
    }
    
    private HttpClientContext httpClientContext;

    private static final Log _log =
            LogFactoryUtil.getLog(LayoutCrawler.class);

    private static final String _rememberMe = Boolean.toString(true);
    
    private String _publicLayoutUrlPrefix;
    private String _privateLayoutUrlPrefix;
    
    private HttpClient _httpClient;

    private HttpClientContext _httpClientContext;
}