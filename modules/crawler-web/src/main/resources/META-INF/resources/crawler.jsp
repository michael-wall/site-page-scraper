<%@ include file="./init.jsp" %>

<%
ConfigTO sitePageCrawlerConfig = (ConfigTO)request.getAttribute("sitePageCrawlerConfig");
%>
<portlet:actionURL var="crawlPagesActionURL" copyCurrentRenderParameters="false" name="/crawlPages" />

<div style="background-color: #FFF;padding-left: 10px;">
	<clay:container-fluid>
		<clay:row>
			<clay:col lg="12" md="12" sm="12" xs="12">
				<br />
				<strong><liferay-ui:message key="site-page-crawler-heading" /></strong><br /><br />
				
				<aui:form name="articleIdForm" action="${crawlPagesActionURL}" method="POST" autocomplete="off">
				
					<aui:input type="checkbox" name="webContentDisplayWidgetLinksOnly" label="web-content-display-widget-links-only" value="<%= sitePageCrawlerConfig.isWebContentDisplayWidgetLinksOnly() %>" helpMessage="web-content-display-widget-links-only-help-message" />

					<aui:input type="checkbox" name="runAsGuestUser" label="run-as-guest-user" value="<%= sitePageCrawlerConfig.isRunAsGuestUser() %>" helpMessage="run-as-guest-user-help-message" />

					<aui:input type="checkbox" name="useCurrentUsersLocaleWhenRunAsGuestUser" label="use-current-users-locale-when-run-as-guest-user" value="<%= sitePageCrawlerConfig.isUseCurrentUsersLocaleWhenRunAsGuestUser() %>" helpMessage="use-current-users-locale-when-run-as-guest-user-message" />

					<aui:input type="checkbox" name="includePublicPages" label="include-public-pages" value="<%= sitePageCrawlerConfig.isIncludePublicPages() %>" helpMessage="include-public-pages-help-message" />
					
					<aui:input type="checkbox" name="includePrivatePages" label="include-private-pages" value="<%= sitePageCrawlerConfig.isIncludePrivatePages() %>" helpMessage="include-private-pages-help-message" />
					
					<aui:input type="checkbox" name="includeHiddenPages" label="include-hidden-pages" value="<%= sitePageCrawlerConfig.isIncludeHiddenPages() %>" helpMessage="include-hidden-pages-help-message" />
					
					<aui:input type="checkbox" name="checkPageGuestRoleViewPermission" label="check-page-guest-role-view-permission" value="<%= sitePageCrawlerConfig.isCheckPageGuestRoleViewPermission() %>" helpMessage="check-page-guest-role-view-permission-help-message" />
					
					<aui:input type="checkbox" name="validateLinksOnPages" label="validate-links-on-pages" value="<%= sitePageCrawlerConfig.isValidateLinksOnPages() %>" helpMessage="validate-links-on-pages-help-message" />

					<aui:input type="checkbox" name="skipExternalLinks" label="skip-external-links" value="<%= sitePageCrawlerConfig.isSkipExternalLinks() %>" helpMessage="skip-external-links-help-message" />

					<span style="display: inline-block;vertical-align: top; padding-top: 10px;">
						<clay:button small="true" type="submit" name="run-site-page-crawler" label="run-site-page-crawler" icon="pages-tree" />
					</span>
				</aui:form>
			</clay:col>
		</clay:row>
	</clay:container-fluid>
	<br />
</div>