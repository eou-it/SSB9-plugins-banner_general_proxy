<!DOCTYPE html>
<%--
/*******************************************************************************
Copyright 2019 Ellucian Company L.P. and its affiliates.
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

        <asset:javascript src="modules/proxyMgmt-application-mf.js"/>
        <asset:stylesheet src="modules/proxyMgmt-applicationLTR-mf.css"/>

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
    <div id="content" ng-app="proxyManagementApp" class="container-fluid proxy" aria-relevant="additions" role="main">
        <div ui-view class="gen-home-main-view"></div>
    </div>
<div class="body-overlay"></div>
</body>
</html>