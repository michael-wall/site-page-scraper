package com.mw.site.crawler;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.net.URI;
import java.util.Locale;

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

    public LayoutCrawler(String publicLayoutUrlPrefix, String privateLayoutUrlPrefix, String idEnc, String passwordEnc, String cookieDomain, Locale locale) {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_USER_AGENT).build();
        
        _cookieDomain = cookieDomain;
        _publicLayoutUrlPrefix = publicLayoutUrlPrefix;
        _privateLayoutUrlPrefix = privateLayoutUrlPrefix;
        
        _autoUserIdEnc = idEnc;
        _autoPasswordEnc = passwordEnc;
        
        httpClientContext = _getHttpClientContext(_cookieDomain, locale);    
    }
    
    public String[] validateLink(String url, String relativeUrlPrefix, Locale locale) {	
    	String[] responseStringArray = {"", ""};
    	HttpEntity entity = null;
    	
        try {
            int connectTimeout = 10000; // 10 seconds
            int connectionRequestTimeout = 10000; // 10 seconds
            int socketTimeout = 10000; // 10 seconds

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout) // time to establish connection with the server
                    .setConnectionRequestTimeout(connectionRequestTimeout) // time to get connection from pool
                    .setSocketTimeout(socketTimeout) // time waiting for data
                    .build();
            
            URI uri = new URI(url);
            
            if (!uri.isAbsolute()) {
            	url = relativeUrlPrefix + url;
            }
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);

            HttpResponse httpResponse = _httpClient.execute(httpGet, httpClientContext);

            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();
                       
            responseStringArray[0] = "" + statusLine.getStatusCode();
            responseStringArray[1] = "" + statusLine.getReasonPhrase();

            return responseStringArray;
        } catch (Exception exception) {
        	_log.info("Exception validating url: " + url +", " + exception.getMessage());
        	
        	responseStringArray[0] = "-1";
        	responseStringArray[1] = exception.getMessage();
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
            if (layout.isPrivateLayout()) {
            	layoutFullURL = _privateLayoutUrlPrefix + layout.getFriendlyURL(locale);
            } else {
            	layoutFullURL = _publicLayoutUrlPrefix + layout.getFriendlyURL(locale);
            }

            HttpGet httpGet = new HttpGet(layoutFullURL);
            
            HttpResponse httpResponse = _httpClient.execute(httpGet, httpClientContext);

            //List<Cookie> cookies = httpClientContext.getCookieStore().getCookies();
            
            //for (Cookie cookie: cookies) {
            //	_log.info(cookie.getDomain() + ", " + cookie.getName() + ", value: " + cookie.getValue());
        	//}
            
            StatusLine statusLine = httpResponse.getStatusLine();
            
            entity = httpResponse.getEntity();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            	responseStringArray[0] = layoutFullURL;
            	responseStringArray[1] = EntityUtils.toString(entity);
            	
                return responseStringArray;
            } 
        }
        catch (Exception exception) {
        	 _log.info("Unable to crawl layout: " + layoutFullURL + ", Exception:" +  exception.getMessage());
        } finally {
        	try {
        		if (entity != null) EntityUtils.consume(entity);
        	} catch (Exception e) {}
        }

        return null;
    }

    private HttpClientContext _getHttpClientContext(String hostName, Locale locale) {

        if (_httpClientContext != null) {
            return _httpClientContext;
        }

        CookieStore cookieStore = new BasicCookieStore();
       
        BasicClientCookie autoIdClientCookie =
                _createClientCookie(CookieKeys.ID, _autoUserIdEnc, hostName);
        BasicClientCookie autoPasswordClientCookie =
                _createClientCookie(CookieKeys.PASSWORD, _autoPasswordEnc, hostName);
        BasicClientCookie rememberMeClientCookie =
                _createClientCookie(CookieKeys.REMEMBER_ME, _rememberMe, hostName);

        BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(CookieKeys.GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale), _cookieDomain);
        
        cookieStore.addCookie(autoIdClientCookie);
        cookieStore.addCookie(autoPasswordClientCookie);
        cookieStore.addCookie(rememberMeClientCookie);
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

    private HttpClientContext httpClientContext;

    private static final Log _log =
            LogFactoryUtil.getLog(LayoutCrawler.class);

    private static final String _rememberMe = Boolean.toString(true);

    private static final String _USER_AGENT = "Liferay Page Crawler";
    
    private String _publicLayoutUrlPrefix;
    private String _privateLayoutUrlPrefix;

    private String _autoPasswordEnc;
    private String _autoUserIdEnc;
    private String _cookieDomain;
    
    private HttpClient _httpClient;

    private HttpClientContext _httpClientContext;
}