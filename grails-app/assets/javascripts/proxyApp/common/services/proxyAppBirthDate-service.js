/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyApp.service('proxyAppBirthDateService', ['proxyAppDateService',
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
        }
    }
]);
