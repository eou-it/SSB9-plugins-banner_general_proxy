<!DOCTYPE html>
<%--
/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <g:applyLayout name="bannerWebPage">
        <title><g:message code="banner.general.common.title"/></title>

        <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
        <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
    %{--<meta name="menuDefaultBreadcrumbId" content=""/>--}%
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <META HTTP-EQUIV="X-Frame-Options" CONTENT="deny">
        <g:set var="appName" value= "${System.properties['BANNERXE_APP_NAME']}"/>
        <g:set var="mep" value="${params?.mepCode}"/>
        <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0"/>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">

    </g:applyLayout>


    <script type="text/javascript">
        <g:i18n_setup/>
    </script>
    <!--[if lte IE 8]>
      <script>
        document.createElement('ng-include');
        document.createElement('ng-pluralize');
        document.createElement('ng-view');


      </script>
    <![endif]-->
</head>

<body>

<div class=" container wrapper" id="content" class="container-fluid" aria-live="polite" aria-atomic="true"
        aria-relevant="additions" role="main">

    <br>
    <br>
    <br>
    <br>
    <br>
    <h1><a class="breadcrumbButton leaf-breadcrumb" data-id="2" href=${createLink(uri: '/ssb/directDeposit')}>
        Banner Direct Deposit App</a></h1>
    <br>
    <h1><a class="breadcrumbButton leaf-breadcrumb" data-id="2" href=${createLink(uri: '/ssb/personalInformation')}>
        Banner Person Profile App</a></span></h1>

</div>
</body>

</html>
<script type="text/javascript">
    function showPage(element) {
        //alert('show' + element);
        window.location.href=$(element).attr('data-url');R
    }
</script>
