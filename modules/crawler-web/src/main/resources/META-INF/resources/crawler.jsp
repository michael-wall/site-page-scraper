<%@ include file="./init.jsp" %>

<%
boolean sitePageCrawlerTriggered = (boolean)request.getAttribute("sitePageCrawlerTriggered");
boolean sitePageCrawlerNoPagesFound = (boolean)request.getAttribute("sitePageCrawlerNoPagesFound");

String sitePageCrawlerStartTime = (String)request.getAttribute("sitePageCrawlerStartTime");
%>
<portlet:actionURL var="crawlPagesActionURL" copyCurrentRenderParameters="false" name="/crawlPages" />

<div style="background-color: #FFF;padding-left: 10px;">
	<clay:container-fluid>
		<clay:row>
			<clay:col lg="12" md="12" sm="12" xs="12">
				<br />
				<liferay-ui:message key="site-page-crawler-heading" /><br /><br />
			
				<aui:button cssClass="btn-sm" type="submit" href="<%= crawlPagesActionURL %>" name="run-site-page-crawler" value="run-site-page-crawler" /><br /><br />
				
				<c:if test="${sitePageCrawlerTriggered and not empty sitePageCrawlerStartTime}">
					<strong><liferay-ui:message key="site-page-crawler-triggered" arguments="${sitePageCrawlerStartTime}" /></strong>
				</c:if>
				<c:if test="${sitePageCrawlerNoPagesFound}">
					<strong><liferay-ui:message key="site-page-crawler-no-pages-found" /></strong>
				</c:if>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
	<br />
</div>