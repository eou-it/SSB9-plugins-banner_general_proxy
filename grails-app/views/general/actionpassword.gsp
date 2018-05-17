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
            Action Password:
            <input type="text" name="token">
            <input type="submit" value="Submit" class="signin-button">
            <br>

            <div class="actionpassword">
                The email you received contained an Action Link(which you have used to get here)
                and an Action Password used to verify that you are an intended  recipient.
                You will also use this value as an 'Old Pin' on the following page
                when establishing your new Pin.
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