<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'generalSsbMain.css')}"/>
</head>

<body class="pageBg">

<div class="splashBg">
    <form action="submitActionPassword" method="post">
        <div class="appNameProxy">Banner<span>&reg;</span></div>

        <div class="logInProxy appNameProxy">
            <g:message code="banner.generalssb.submitactionpassword.label"/>
            <input type='password' name="p_verify">
            <input type="hidden" name="token" value="${token}" />
            <input type="hidden" name="gidm" value="${gidm}" />
            <input type="submit" value="Submit" class="signin-button">
            <br>

            <div class="actionpassword">
                <g:message code="banner.generalssb.submitactionpassword"/>
                <g:if test='${flash.message}'>
                    <span class="icon-error"></span>
                    ${flash.message}
                </g:if>
            </div>
        </div>
    </form>


    <div class="copyright">
        <p>&copy; <g:message code="net.hedtech.banner.login.copyright1"/></p>

        <p><g:message code="net.hedtech.banner.login.copyright2"/></p>
    </div>
</div>
</body>
</html>