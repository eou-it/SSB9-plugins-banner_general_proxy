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
        <g:if test='${flash.message}'>
            <span class="icon-error"></span ><span class="actionpassword">${flash.message}</span>
        </g:if>
        <table>
     <tr>
            <td><label><g:message code="proxy.passwordManagement.email.address.label"/> </label> <span class="red-star"> *</span></td>
            <td><input type="text" name="p_email" required></td>
     </tr>
            <tr>
                <td><label><g:message code="proxy.passwordManagement.initialPassword.label"/> </label> <span class="red-star"> *</span></td>
                <td><input type="password" name="p_pin_orig" required></td>
            </tr>
    <tr>
        <td><label><g:message code="proxy.passwordManagement.newPassword.label"/> </label> <span class="red-star"> *</span></td>
        <td><input type="password" name="p_pin1" required></td>
    </tr>

            <tr>
                <td><label><g:message code="proxy.passwordManagement.verifyNewPassword.label"/> </label> <span class="red-star"> *</span></td>
                <td><input type="password" name="p_pin2" required></td>
                <td>    <input type="submit" value="Save" class="signin-button"></td>

            </tr>

    </table>
       <br>
        <div class="actionpassword">
            <g:message code="proxy.passwordManagement.changePassword"/>
        </div>

    </div>
    <input type="hidden" name="gidm" value="${gidm}" />
</form>
</p>


</div>


</body>
</html>