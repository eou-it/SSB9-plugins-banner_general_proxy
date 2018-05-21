<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'generalSsbMain.css')}"/>
    <title>Form</title>
</head>
<body class="pageBg">
<div class="splashBg">
<form controller="general" action="resetPinAction" method="post">

    <div class="appNameProxy">Banner<span>&reg;</span></div>

    <div class="logInProxy appNameProxy">
        <table>
     <tr>
            <td><label>Email Address: </label> <span class="red-star"> *</span></td>
            <td><input type="text" name="email" required></td>
     </tr>
            <tr>
                <td><label>Old Pin: </label> <span class="red-star"> *</span></td>
                <td><input type="text" name="oldPin" required></td>
            </tr>
    <tr>
        <td><label>New Pin: </label> <span class="red-star"> *</span></td>
        <td><input type="text" name="newPin" required></td>
    </tr>

            <tr>
                <td><label>Validate Pin: </label> <span class="red-star"> *</span></td>
                <td><input type="text" name="validatePin" required></td>
                <td>    <input type="submit" value="Save" class="signin-button"></td>

            </tr>

    </table>
       <br>
        <div class="actionpassword">
            <g:message code="banner.generalssb.pin"/>
        </div>

    </div>

</form>
</p>


</div>


</body>
</html>