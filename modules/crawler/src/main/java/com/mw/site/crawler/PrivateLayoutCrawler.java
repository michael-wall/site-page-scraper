package com.mw.site.crawler;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

public class PrivateLayoutCrawler {

    public PrivateLayoutCrawler(String layoutUrlPrefix, String idEnc, String passwordEnc, String cookieDomain) {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        _httpClient = httpClientBuilder.setUserAgent(_USER_AGENT).build();
        
        _cookieDomain = cookieDomain;
        _layoutUrlPrefix = layoutUrlPrefix;

        initializeCredentials(idEnc, passwordEnc);
    }

    public String[] getLayoutContent(Layout layout, Locale locale) {
    	String[] responseStringArray = {"", ""};

        try {
            HttpClientContext httpClientContext =
                    _getBasicHttpClientContext(_cookieDomain);

            BasicClientCookie guestLanguageIdClientCookie = _createClientCookie(
                    CookieKeys.GUEST_LANGUAGE_ID, LocaleUtil.toLanguageId(locale),
                    _cookieDomain);

            httpClientContext.getCookieStore().addCookie(
                    guestLanguageIdClientCookie);

            String layoutFullURL = _layoutUrlPrefix + layout.getFriendlyURL(locale);

            HttpGet httpGet =
                    new HttpGet(layoutFullURL);

            HttpResponse httpResponse =
                    _httpClient.execute(httpGet, httpClientContext);

            StatusLine statusLine = httpResponse.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            	responseStringArray[0] = layoutFullURL;
            	responseStringArray[1] = EntityUtils.toString(httpResponse.getEntity());
            	
                return responseStringArray;
            }
            else {
                if (_log.isWarnEnabled()) {
                    _log.warn("HttpStatus " + statusLine.getStatusCode());
                }
            }
        }
        catch (Exception exception) {
            if (_log.isWarnEnabled()) {
                _log.warn("Unable to crawl layout content for layout: " + layout.getFriendlyURL(locale), exception);
            }
        }

        return null;
    }

    private HttpClientContext _getBasicHttpClientContext(String hostName) {

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

        cookieStore.addCookie(autoIdClientCookie);
        cookieStore.addCookie(autoPasswordClientCookie);
        cookieStore.addCookie(rememberMeClientCookie);

        HttpClientContext httpClientContext = new HttpClientContext();

        httpClientContext.setCookieStore(cookieStore);

        _httpClientContext = httpClientContext;

        return httpClientContext;
    }

    private BasicClientCookie _createClientCookie(
            String cookieName, String cookieValue, String domain) {

        BasicClientCookie basicClientCookie =
                new BasicClientCookie(cookieName, cookieValue);

        basicClientCookie.setDomain(domain);

        return basicClientCookie;
    }

    protected void initializeCredentials(String idEnc, String passwordEnc) {

        _autoUserIdEnc = idEnc;
        _autoPasswordEnc = passwordEnc;
    }

    public static final String COOKIE_KEYS_CRAWLER_HASH =
            "COOKIE_KEYS_CRAWLER_HASH";

    private static final Log _log =
            LogFactoryUtil.getLog(PrivateLayoutCrawler.class);

    private static final String _rememberMe = Boolean.toString(true);

    private static final String _USER_AGENT = "Liferay Page Crawler";
    
    private String _layoutUrlPrefix;

    private String _autoPasswordEnc;
    private String _autoUserIdEnc;
    private String _cookieDomain;
    
    private HttpClient _httpClient;

    private HttpClientContext _httpClientContext;
}