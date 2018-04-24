<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <title>Form</title>
</head>
<body class="pageBg">
<div class="splashBg">
<form controller="general" action="resetPinAction" method="post">

    <div class="appName">Banner<span>&reg;</span></div>

    <div class="logIn appName">
        <table>
     <tr>
            <td><label>Email Address: </label></td>
            <td><input type="text" name="email"></td>
     </tr>
            <tr>
                <td><label>Old Pin: </label></td>
                <td><input type="text" name="oldPin"></td>
            </tr>
    <tr>
        <td><label>New Pin: </label></td>
        <td><input type="text" name="newPin"></td>
    </tr>

            <tr>
                <td><label>Validate Pin: </label></td>
                <td><input type="text" name="validatePin"></td>
                <td>    <input type="submit" value="Sign In" class="signin-button"></td>

            </tr>
    </div>

    </table>

</form>


</div>


</body>
</html>