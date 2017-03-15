<!DOCTYPE html>
<%--
/*******************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<!--[if IE 9 ]>    <html xmlns:ng="http://angularjs.org" ng-app="generalSsbApp" id="ng-app" class="ie9"> <![endif]-->
<html xmlns:ng="http://angularjs.org" ng-app="generalSsbApp" id="ng-app">
<head>
    <g:applyLayout name="bannerWebPage">
        <title><g:message code="banner.general.common.title"/></title>

        <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
        <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <g:set var="appName" value= "${System.properties['BANNERXE_APP_NAME']}"/>
        <g:set var="mep" value="${params?.mepCode}"/>
        <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0"/>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">

        <g:if test="${message(code: 'default.language.direction')  == 'rtl'}">
            <r:require modules="generalSsbAppRTL"/>
        </g:if>
        <g:else>
            <r:require modules="generalSsbAppLTR"/>
        </g:else>

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
            // URL pattern to exclude from updating genMainCallingPage.  No breadcrumbs for "/BannerGeneralSsb/" URLs
            // should be created for the landing page.
            var referrerUrl = document.referrer,
                excludedRegex = /\/BannerGeneralSsb\//,
                isExcluded;

            if (referrerUrl) {
                isExcluded = excludedRegex.test(referrerUrl);

                if (!isExcluded) {
                    // Track this page
                    sessionStorage.setItem('genMainCallingPage', referrerUrl);
                }
            }
        })();
    </script>
</head>

<body>

<div class="body-overlay"></div>
<svg class="hide-svg" role="presentation">
    <defs>
        <clipPath id="profilePicClip">
            <circle cx="33" cy="40" r="33"/>
        </clipPath>
    </defs>
</svg>
<div id="content" class="container-fluid" aria-relevant="additions" role="main">
    <div ui-view class="gen-home-main-view"></div>
</div>
<script  type="text/javascript">
    function tellAngular() {
        var domElt = document.getElementsByClassName('page-header');
        scope = angular.element(domElt).scope();
        scope.$apply(function() {
            if(window.innerWidth > 768)
            {
                scope.searchView = false;
            }
        });
    }
    window.onresize = tellAngular;
</script>
</body>
</html>
