%{--*******************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************--}%
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10" />

    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <asset:stylesheet href="rtl-login.css"/>
        <asset:stylesheet href="login-rtl.css"/>
        <asset:stylesheet href="rtl-login-patch.css"/>
        <asset:stylesheet href="proxy-rtl.css"/>
        <asset:stylesheet href="proxy-patch-rtl.css"/>
        <asset:stylesheet href="eds-rtl.css"/>
    </g:if>
    <g:else>
        <asset:stylesheet href="login.css"/>
        <asset:stylesheet href="proxy.css"/>
        <asset:stylesheet href="eds.css"/>
    </g:else>

    <g:theme/>

    <asset:link rel="apple-touch-icon" sizes="57x57" href="eds/apple-touch-icon-57x57.png"/>
    <asset:link rel="apple-touch-icon" sizes="60x60" href="eds/apple-touch-icon-60x60.png"/>
    <asset:link rel="apple-touch-icon" sizes="72x72" href="eds/apple-touch-icon-72x72.png"/>
    <asset:link rel="apple-touch-icon" sizes="76x76" href="eds/apple-touch-icon-76x76.png"/>
    <asset:link rel="apple-touch-icon" sizes="114x114" href="eds/apple-touch-icon-114x114.png"/>
    <asset:link rel="apple-touch-icon" sizes="120x120" href="eds/apple-touch-icon-120x120.png"/>
    <asset:link rel="apple-touch-icon" sizes="144x144" href="eds/apple-touch-icon-144x144.png"/>
    <asset:link rel="apple-touch-icon" sizes="152x152" href="eds/apple-touch-icon-152x152.png"/>
    <asset:link rel="apple-touch-icon" sizes="180x180" href="eds/apple-touch-icon-180x180.png"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-32x32.png" sizes="32x32"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/android-chrome-192x192.png" sizes="192x192"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-96x96.png" sizes="96x96"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-16x16.png" sizes="16x16"/>
    <asset:link rel="shortcut icon"  sizes="57x57" href="eds/favicon.ico" type="image/x-icon"/>

</head>
<body class="pageBg proxy-pin-mgmt">
<div class="splashBg proxy-pin-mgmt reset-pin">
    <form controller="general" action="resetPinAction" method="post" class="reset-pin-form">

        <div class="appName">Banner<span>&reg;</span></div>
        <div class="ellucianName"></div>

        <div class="reset-input-section">
            <div class="loginMsgReset">
                <g:message code="proxy.passwordManagement.resetMsg"/>
            </div>
            <br>
            <div>
                <p class="reset-msg">
                    <g:message code="proxy.passwordManagement.changePassword"/>
                </p>

                <g:if test='${flash.message}'>
                    <span class="icon-error"></span ><span class="actionpassword">
                    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">&nbsp</g:if> <!--This adds the missing space before icon in RTL-->
                    ${flash.message}</span>
                </g:if>
            </div>
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
