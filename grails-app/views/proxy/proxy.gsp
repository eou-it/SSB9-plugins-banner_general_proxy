<!DOCTYPE html>
<%--
/*******************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<!--[if IE 9 ]>    <html xmlns:ng="http://angularjs.org" ng-app="proxyApp" id="ng-app" class="ie9"> <![endif]-->
<html xmlns:ng="http://angularjs.org" id="ng-app">
<head>
    <g:applyLayout name="bannerSelfServicePage">
        <title><g:message code="banner.general.common.title"/></title>

        <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
        <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="locale" content="${request.locale.toLanguageTag()}" >
        <g:set var="applicationContextRoot" value= "${application.contextPath}"/>
        <meta name="applicationContextRoot" content="${applicationContextRoot}">
        <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0"/>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <g:set var="guestUser" value="${org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute('guestUser')}"/>

        <g:if test="${guestUser}">
            <g:if test="${message(code: 'default.language.direction')  == 'rtl'}">
                <r:require modules="proxyAppRTL"/>
            </g:if>
            <g:else>
                <r:require modules="proxyAppLTR"/>
            </g:else>
        </g:if>

    </g:applyLayout>

    <script type="text/javascript">
        <g:i18n_setup/>
    </script>

    <script>
        document.createElement('ng-include');
        document.createElement('ng-pluralize');
        document.createElement('ng-view');
    </script>
    <script type="text/javascript">
        // Track calling page for breadcrumbs
        (function () {
            // URLs to exclude from updating genMainCallingPage.  No breadcrumbs for "/BannerGeneralSsb/" URLs
            // (i.e. the applicationName variable below) or App Nav should be created for the landing page.
            var referrerUrl = document.referrer,
                    excludedRegex = [
                        /\${applicationContextRoot}\//,
                        /\/seamless/
                    ],
                    isExcluded;

            if (referrerUrl) {
                isExcluded = _.find(excludedRegex, function (regex) {
                    return regex.test(referrerUrl);
                });

                if (!isExcluded) {
                    // Track this page
                    sessionStorage.setItem('proxyCallingPage', referrerUrl);
                }
            }
        })();
    </script>
</head>

<body>

    <div id="content" ng-app="proxyApp" ng-controller="gssLandingPageController" class="container-fluid proxy" aria-relevant="additions" role="main" ng-init="initLandingPage(${proxyProfile})">
        <div ui-view class="gen-home-main-view"></div>
    </div>
<div class="body-overlay"></div>
</body>
</html>
