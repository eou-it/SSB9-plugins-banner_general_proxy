<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
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
        <div class="appName">Banner<span>&reg;</span></div>

        <div class="flex-wrapper">
            <div class="ellucianName"></div>

            <!-- div class="actionPW-content" -->
                <div class="loginMsg" id="loginMsg">
                    Enter your Action Password, then click Submit to continue.
                </div>

                <g:if test='${flash.message}'>
                    <div class="loginMsg">
                        <span class="icon-error"></span>${flash.message}
                    </div>
                </g:if>


                <form action="submitActionPassword" method="post">

                    <div class="actionPw-form">
                        <input class="eds-text-field action-input" type='password' name="p_verify"
                               placeholder="<g:message code="proxy.passwordManagement.initialPassword.label"/>">
                        <input type="hidden" name="token" value="${token}" />
                        <input type="hidden" name="gidm" value="${gidm}" />
                        <input type="submit" value="<g:message code="proxy.label.submit"/>" class="primary submit-btn">
                        <br>

                    </div>
                </form>
                <p class="actionPW-text">
                    <g:message code="proxy.passwordManagement.submitInitialPassword"/>
                </p>
            <!-- /div -->
        </div>
        <div class="subsection-divider"><span></span></div>
        <div class="copyright">
            <p>&copy; <g:message code="net.hedtech.banner.login.copyright1"/></p>

            <p><g:message code="net.hedtech.banner.login.copyright2"/></p>
        </div>
    </div>
</body>
</html>