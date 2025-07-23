<%@ include file="./init.jsp" %>

<%
boolean sitePageCrawlerTriggered = (boolean)request.getAttribute("sitePageCrawlerTriggered");
String sitePageCrawlerStartTime = (String)request.getAttribute("sitePageCrawlerStartTime");
%>
<portlet:actionURL var="crawlPagesActionURL" copyCurrentRenderParameters="false" name="/crawlPages" />

<p>
	<liferay-ui:message key="click-the-run-site-page-crawler-button-to-trigger-the-asynchronous-page-crawler" /><br /><br />

	<aui:button cssClass="btn-sm" type="submit" href="<%= crawlPagesActionURL %>" name="run-site-page-crawler" value="run-site-page-crawler" /><br /><br />
	
	<c:if test="${sitePageCrawlerTriggered and not empty sitePageCrawlerStartTime}">
		<strong><liferay-ui:message key="site-page-crawler-triggered" arguments="${sitePageCrawlerStartTime}" /></strong>
	</c:if>
</p>