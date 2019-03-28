/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service('proxyEmailService', ['notificationCenterService',
    function (notificationCenterService) {
        var invalidCharRegEx = /[ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]/i,
            validEmailRegEx = /[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+@[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+\.[A-Z]{2,}/i;

        this.getErrorEmailAddress = function (email) {
            return invalidCharRegEx.test(email) ? 'proxy.personalinformation.error.invalidEmailChars' : this.getErrorEmailAddressFormat(email);
        };

        this.getErrorEmailAddressFormat = function (email) {
            return validEmailRegEx.test(email) ? null : 'proxy.personalinformation.error.invalidEmailFormat';
        };
    }
]);
