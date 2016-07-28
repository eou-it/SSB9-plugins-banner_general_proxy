<%-- Copyright 2013 Ellucian Company L.P. and its affiliates. --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0"/>
    <g:set var="mep" value="${params?.mepCode}"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <g:set var="appName" value= "${System.properties['BANNERXE_APP_NAME']}"/>
    <g:if test="${appName.equals('DirectDeposit')}">
        <meta HTTP-EQUIV="REFRESH" content="0; url=${!mep ? 'ssb/directDeposit' : 'ssb/directDeposit?mepCode='+mep}">
    </g:if>
    <g:elseif test="${appName.equals('PersonProfile')}">
        <meta HTTP-EQUIV="REFRESH" content="0; url=${!mep ? 'ssb/personalInformation' : 'ssb/personalInformation?mepCode='+mep}">
    </g:elseif>
    <g:else>
        <meta HTTP-EQUIV="REFRESH" content="0; url=${!mep ? 'ssb/general' : 'ssb/general?mepCode='+mep}">
    </g:else>
</head>
<body>
</body>
</html>
