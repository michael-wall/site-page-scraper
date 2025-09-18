<%@ include file="./init.jsp" %>

<%
int publicVirtualHostCount = (int) request.getAttribute("publicVirtualHostCount");
int privateVirtualHostCount = (int) request.getAttribute("privateVirtualHostCount");
long publicPageCount = (long) request.getAttribute("publicPageCount");
long privatePageCount = (long) request.getAttribute("privatePageCount");
String origin = (String) request.getAttribute("origin");
String currentUserLocaleLabel = (String) request.getAttribute("currentUserLocaleLabel");
String defaultLocaleLabel = (String) request.getAttribute("defaultLocaleLabel");
ConfigTO sitePageCrawlerConfig = (ConfigTO)request.getAttribute("sitePageCrawlerConfig");
%>
<c:set var="runAsGuestLabel"><liferay-ui:message key="run-as-guest-user-with-x-locale" arguments="<%= new Object[] {defaultLocaleLabel} %>" translateArguments="false" /></c:set>
<c:set var="useCurrentUsersLocaleWhenRunAsGuestUserLabel"><liferay-ui:message key="use-current-users-locale-x-when-run-as-guest-user" arguments="<%= new Object[] {currentUserLocaleLabel} %>" translateArguments="false" /></c:set>

<portlet:actionURL var="crawlPagesActionURL" copyCurrentRenderParameters="false" name="/crawlPages" />

<div style="background-color: #FFF;padding-left: 10px;">
	<clay:container-fluid>
		<clay:row>
			<clay:col lg="12" md="12" sm="12" xs="12">
				<br />
				<strong><liferay-ui:message key="site-page-crawler-heading" /></strong><br /><br />
				<c:choose>
					<c:when test="<%= publicVirtualHostCount > 0 %>">
						<strong><liferay-ui:message key="site-public-page-count-is-x-and-the-site-has-x-public-page-virtual-hosts-defined" arguments="<%= new Object[] {publicPageCount, publicVirtualHostCount} %>" translateArguments="false" /></strong><br />
					</c:when>
					<c:otherwise>
						<strong><liferay-ui:message key="site-public-page-count-is-x" arguments="<%= new Object[] {publicPageCount} %>" translateArguments="false" /></strong><br />
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="<%= privateVirtualHostCount > 0 %>">
						<strong><liferay-ui:message key="site-private-page-count-is-x-and-the-site-has-x-private-page-virtual-hosts-defined" arguments="<%= new Object[] {privatePageCount, privateVirtualHostCount} %>" translateArguments="false" /></strong><br />
					</c:when>
					<c:otherwise>
						<strong><liferay-ui:message key="site-private-page-count-is-x" arguments="<%= new Object[] {privatePageCount} %>" translateArguments="false" /></strong><br />
					</c:otherwise>
				</c:choose>
				<strong><liferay-ui:message key="the-tool-will-run-with-the-current-hostname-of-x" arguments="<%= new Object[] {origin} %>" translateArguments="false" /></strong><br />
				<c:if test="<%= publicVirtualHostCount > 0 && privateVirtualHostCount > 0 %>">
						<strong><liferay-ui:message key="consider-running-the-tool-separately-for-the-public-and-private-pages-using-the-most-appropriate-hostname-for-each" arguments="<%= new Object[] {origin} %>" translateArguments="false" /></strong><br />
				</c:if>
				<br />
				<aui:form name="articleIdForm" action="${crawlPagesActionURL}" method="POST" autocomplete="off">
				
					<aui:input type="checkbox" name="webContentDisplayWidgetLinksOnly" label="web-content-display-widget-links-only" value="<%= sitePageCrawlerConfig.isWebContentDisplayWidgetLinksOnly() %>" helpMessage="web-content-display-widget-links-only-help-message" />

					<aui:input type="checkbox" name="runAsGuestUser" label="${runAsGuestLabel}" value="<%= sitePageCrawlerConfig.isRunAsGuestUser() %>" helpMessage="run-as-guest-user-help-message" />

					<aui:input type="checkbox" name="useCurrentUsersLocaleWhenRunAsGuestUser" label="${useCurrentUsersLocaleWhenRunAsGuestUserLabel}" value="<%= sitePageCrawlerConfig.isUseCurrentUsersLocaleWhenRunAsGuestUser() %>" helpMessage="use-current-users-locale-when-run-as-guest-user-message" />

					<aui:input type="checkbox" name="includePublicPages" label="include-public-pages" value="<%= sitePageCrawlerConfig.isIncludePublicPages() %>" helpMessage="include-public-pages-help-message" />
					
					<aui:input type="checkbox" name="includePrivatePages" label="include-private-pages" value="<%= sitePageCrawlerConfig.isIncludePrivatePages() %>" helpMessage="include-private-pages-help-message" />
					
					<aui:input type="checkbox" name="includeHiddenPages" label="include-hidden-pages" value="<%= sitePageCrawlerConfig.isIncludeHiddenPages() %>" helpMessage="include-hidden-pages-help-message" />
					
					<aui:input type="checkbox" name="checkPageGuestRoleViewPermission" label="check-page-guest-role-view-permission" value="<%= sitePageCrawlerConfig.isCheckPageGuestRoleViewPermission() %>" helpMessage="check-page-guest-role-view-permission-help-message" />
					
					<aui:input type="checkbox" name="validateLinksOnPages" label="validate-links-on-pages" value="<%= sitePageCrawlerConfig.isValidateLinksOnPages() %>" helpMessage="validate-links-on-pages-help-message" />

					<aui:input type="checkbox" name="skipExternalLinks" label="skip-links-using-any-other-hostname" value="<%= sitePageCrawlerConfig.isSkipExternalLinks() %>" helpMessage="skip-links-using-any-other-hostname-help-message" />

					<span style="display: inline-block;vertical-align: top; padding-top: 10px;">
						<clay:button small="true" type="submit" name="run-site-page-crawler" label="run-site-page-crawler" icon="pages-tree" />
					</span>
				</aui:form>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
	<br />
</div>