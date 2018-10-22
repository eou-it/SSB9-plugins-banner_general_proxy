<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10" />

    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerGeneralProxy', dir: 'css', file: 'proxy.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerUiSs', dir: 'css', file: 'eds.css')}"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'rtl-login.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerGeneralProxy', dir: 'css', file: 'proxy-rtl.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerUiSs', dir: 'css', file: 'eds-rtl.css')}"/>
    </g:if>
</head>
<body class="pageBg actionPW">
<div class="splashBg actionPW">
    <form controller="general" action="resetPinAction" method="post">

        <div class="appName">Banner<span>&reg;</span></div>
        <div class="ellucianName"></div>

        <div class=""> <!-- logInProxy -->
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

            <input class="primary submit-btn" type="submit" value="<g:message code="proxy.passwordManagement.signIn"/>">
        </div>
        <input type="hidden" name="gidm" value="${gidm}" />
    </form>
</div>

</body>
</html>