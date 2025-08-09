<%@ include file="./init.jsp" %>

<%
boolean sitePageCrawlerTriggered = (boolean)request.getAttribute("sitePageCrawlerTriggered");
boolean sitePageCrawlerNoPagesFound = (boolean)request.getAttribute("sitePageCrawlerNoPagesFound");
String sitePageCrawlerStartTime = (String)request.getAttribute("sitePageCrawlerStartTime");
%>

<portlet:renderURL var="homeRenderURL" copyCurrentRenderParameters="false">
		<portlet:param name="mvcRenderCommandName" value="/home"/>
</portlet:renderURL>

<div style="background-color: #FFF;padding-left: 10px;">
	<clay:container-fluid>
		<clay:row>
			<clay:col lg="12" md="12" sm="12" xs="12">
				<br />
				<strong><liferay-ui:message key="site-page-crawler-response-heading" /></strong><br /><br />
				
				<c:if test="${sitePageCrawlerTriggered and not empty sitePageCrawlerStartTime}">
					<liferay-ui:message key="site-page-crawler-triggered" arguments="${sitePageCrawlerStartTime}" />
				</c:if>
				<c:if test="${sitePageCrawlerNoPagesFound}">
					<liferay-ui:message key="site-page-crawler-no-matching-pages-found" />
				</c:if>
				<br /><br />
				<span style="display: inline-block;vertical-align: top; padding-top: 10px;">
					<clay:button small="true" style="primary" name="home" label="home" icon="home-full" onClick="<%= "location.href='" + homeRenderURL + "'" %>" />
				</span>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
	<br />
</div>