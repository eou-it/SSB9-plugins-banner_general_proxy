<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
</head>
<body class="pageBg">

<div class="splashBg">
<form action="submitActionPassword" method="post">
    <div class="appName">Banner<span>&reg;</span></div>

    <div class="logIn appName">
    Action Password:
    <input type="text" name="firstname">
    <input type="submit" value="Sign" class="signin-button">
    </div>
</form>


<div class="copyright">
    <p>&copy; <g:message code="net.hedtech.banner.login.copyright1"/></p>

    <p><g:message code="net.hedtech.banner.login.copyright2"/></p>
</div>
</div>
</body>
</html>