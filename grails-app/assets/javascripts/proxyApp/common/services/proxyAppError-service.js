/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyApp.service('proxyAppErrorService', ['proxyAppDateService',
    function (proxyAppDateService) {
        this.getErrorBirthDate = function (date) {
            var ERROR_MESSAGES = [
                    'proxy.personalinformation.onSave.p_birth_date_format_error',
                    'proxy.personalinformation.onSave.p_birth_date_in_future_error'
                ];

            if (date) {
                if (!proxyAppDateService.stringToDate(date)) {
                    return ERROR_MESSAGES[0];
                }
                else if (!proxyAppDateService.dateIsInPast(date)) {
                    return ERROR_MESSAGES[1];
                }
            }
            else {
                return '';
            }
        };

        var invalidCharRegEx = /[ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]/i,
            validEmailRegEx = /[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+@[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+\.[A-Z]{2,}/i;

        this.getErrorEmailAddress = function (email) {
            return !email ? '' : (invalidCharRegEx.test(email) ? 'proxy.personalinformation.error.invalidEmailChars' : this.getErrorEmailAddressFormat(email));
        };

        this.getErrorEmailAddressFormat = function (email) {
            return validEmailRegEx.test(email) ? null : 'proxyManagement.onSave.BADEMAIL';
        };
    }
]);
