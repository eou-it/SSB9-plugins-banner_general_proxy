%{--*******************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************--}%
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10" />

    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerGeneralProxy', dir: 'css', file: 'proxy.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerUiSs', dir: 'css', file: 'eds.css')}"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'rtl-login.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerGeneralProxy', dir: 'css', file: 'proxy-rtl.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerUiSs', dir: 'css', file: 'eds-rtl.css')}"/>
    </g:if>

    <link rel="apple-touch-icon" sizes="57x57" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-57x57.png')}"/>
    <link rel="apple-touch-icon" sizes="60x60" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-60x60.png')}"/>
    <link rel="apple-touch-icon" sizes="72x72" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-72x72.png')}"/>
    <link rel="apple-touch-icon" sizes="76x76" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-76x76.png')}"/>
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-114x114.png')}"/>
    <link rel="apple-touch-icon" sizes="120x120" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-120x120.png')}"/>
    <link rel="apple-touch-icon" sizes="144x144" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-144x144.png')}"/>
    <link rel="apple-touch-icon" sizes="152x152" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-152x152.png')}"/>
    <link rel="apple-touch-icon" sizes="180x180" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-180x180.png')}"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-32x32.png')}" sizes="32x32"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'android-chrome-192x192.png')}" sizes="192x192"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-96x96.png')}" sizes="96x96"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-16x16.png')}" sizes="16x16"/>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon.ico')}" type="image/x-icon" />
    
</head>
<body class="pageBg proxy-pin-mgmt">
<div class="splashBg proxy-pin-mgmt reset-pin">
    <form controller="general" action="resetPinAction" method="post" class="reset-pin-form">

        <div class="appName">Banner<span>&reg;</span></div>
        <div class="ellucianName"></div>

        <div class="reset-input-section">
            <div class="loginMsg">
                <g:message code="proxy.passwordManagement.resetMsg"/>
            </div>
            <p class="reset-msg">
                <g:message code="proxy.passwordManagement.changePassword"/>
            </p>

            <g:if test='${flash.message}'>
                <span class="icon-error"></span ><span class="actionpassword">${flash.message}</span>
            </g:if>

            <input class="eds-text-field reset-input" type="text" name="p_email"
                   placeholder="<g:message code="proxy.passwordManagement.email.address.label"/>" required>
            <input class="eds-text-field reset-input" type="password" name="p_pin_orig"
                   placeholder="<g:message code="proxy.passwordManagement.initialPassword.label"/>" required>
            <input class="eds-text-field reset-input" type="password" name="p_pin1"
                   placeholder="<g:message code="proxy.passwordManagement.newPassword.label"/>" required>
            <input class="eds-text-field reset-input" type="password" name="p_pin2"
                   placeholder="<g:message code="proxy.passwordManagement.verifyNewPassword.label"/>" required>
            <br>
            <input class="primary submit-btn" type="submit" value="<g:message code="proxy.label.submit"/>">
        </div>
        <input type="hidden" name="gidm" value="${gidm}" />
    </form>
</div>

</body>
</html>
