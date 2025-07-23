<%@ include file="./init.jsp" %>

<%
boolean pageCrawlerTriggered = (boolean)request.getAttribute("pageCrawlerTriggered");
%>

	<portlet:actionURL var="crawlPagesActionURL" copyCurrentRenderParameters="false" name="/crawlPages" />

<p>
	<liferay-ui:message key="click-the-run-page-crawler-button-to-trigger-the-asynchronous-page-crawler" /><br /><br />

	<aui:button cssClass="btn-sm" type="submit" href="<%= crawlPagesActionURL %>" name="run-page-crawler" value="run-page-crawler" /><br /><br />
	
	<c:if test="${pageCrawlerTriggered}">
		<liferay-ui:message key="page-crawler-triggered" />
	</c:if>
</p>