<%@ page import="org.springframework.context.i18n.LocaleContextHolder" %>
<!DOCTYPE html>
<%--
/*******************************************************************************
Copyright 2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<!--[if IE 9 ]>    <html xmlns:ng="http://angularjs.org" ng-app="proxyApp" id="ng-app" class="ie9"> <![endif]-->
<html xmlns:ng="http://angularjs.org" id="ng-app">
<head>
    <g:applyLayout name="bannerSelfServicePage">
        <title><g:message code="banner.general.common.title" /></title>

        <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu" />
        <meta name="menuBaseURL" content="${request.contextPath}/ssb" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="locale" content="${LocaleContextHolder.getLocale()}">
        <g:set var="applicationContextRoot" value="${application.contextPath}" />
        <meta name="applicationContextRoot" content="${applicationContextRoot}">
        <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge">

        <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
            <asset:stylesheet src="modules/proxyMgmt-applicationRTL-mf.css" />
        </g:if>
        <g:else>
            <asset:stylesheet src="modules/proxyMgmt-applicationLTR-mf.css" />
        </g:else>

        <asset:javascript src="modules/globalProxy-application-mf.js" />

        <g:theme />
    </g:applyLayout>

    <g:bannerMessages />

    <script>
        document.createElement('ng-include');
        document.createElement('ng-pluralize');
        document.createElement('ng-view');
    </script>
    <script type="text/javascript">
        // Track calling page for breadcrumbs
        (function () {
            // URLs to exclude from updating genAppCallingPage, because they're actually either the authentication
            // page, a part of the Personal Information app, or App Nav, and are not "calling pages."
            var referrerUrl = document.referrer,
                excludedRegex = [
                    /\${applicationContextRoot}\/login\/auth$/,
                    /\${applicationContextRoot}\/ssb\/survey\/survey$/,
                    /\${applicationContextRoot}\/resetPassword\/validateans$/,
                    /\${applicationContextRoot}\/ssb\/personalInformation\/resetPasswordWithSecurityQuestions$/,
                    /\/seamless/
                ],
                isExcluded;

            if (referrerUrl) {
                isExcluded = _.find(excludedRegex, function (regex) {
                    return regex.test(referrerUrl);
                });

                if (!isExcluded) {
                    // Track this page
                    sessionStorage.setItem('genAppCallingPage', referrerUrl);
                }
            }
        })();
    </script>
</head>

<body>
<div id="content" ng-app="globalProxyManagementApp" class="container-fluid proxy" aria-relevant="additions" role="main">
    <div ui-view class="gen-home-main-view"></div>
</div>

<div class="body-overlay"></div>
</body>
</html>
