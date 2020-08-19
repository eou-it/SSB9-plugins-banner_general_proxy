/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.factory('GlobalProxyManagementDataValidator', ['globalProxyMgmtErrorService',
    function (globalProxyMgmtErrorService) {
        return function () {
            return {
                firstNameErrMsg: '',
                lastNameErrMsg:  '',
                emailErrMsg:  '',
                verifyEmailErrMsg: '',
                relationshipErrMsg: '',
                authorizationsErrMsg: '',
                startDateErrMsg: '',
                stopDateErrMsg: '',

                setStartAndStopDateErrors: function(proxy, isSubmit) {
                    var errors = globalProxyMgmtErrorService.getErrorDates(proxy, isSubmit);
                    this.startDateErrMsg = errors[0];
                    this.stopDateErrMsg = errors[1];
                },

                isValidProxyData: function(proxy, isUpdate){
                    globalProxyMgmtErrorService.refreshMessages();
                    if (isUpdate) {
                        // The corresponding fields for these errors are always empty for an update, so shim the error messages.
                        this.firstNameErrMsg = false;
                        this.lastNameErrMsg = false;
                        this.emailErrMsg = false;
                        this.verifyEmailErrMsg = false;
                    } else {
                        this.firstNameErrMsg = globalProxyMgmtErrorService.getErrorFirstName(proxy);
                        this.lastNameErrMsg = globalProxyMgmtErrorService.getErrorLastName(proxy);
                        this.emailErrMsg = globalProxyMgmtErrorService.getErrorEmail(proxy);
                        this.verifyEmailErrMsg = globalProxyMgmtErrorService.getErrorVerifyEmail(proxy);
                    }

                    this.relationshipErrMsg = globalProxyMgmtErrorService.getErrorRelationship(proxy);
                    this.authorizationsErrMsg = globalProxyMgmtErrorService.getErrorAuthorizations(proxy);
                    this.setStartAndStopDateErrors(proxy, true);

                    return !(this.firstNameErrMsg || this.lastNameErrMsg || this.emailErrMsg || this.verifyEmailErrMsg ||
                        this.relationshipErrMsg || this.authorizationsErrMsg || this.startDateErrMsg || this.stopDateErrMsg);
                },

                removeProxyProfileFieldErrors: function (proxy) {
                    if (this.firstNameErrMsg) {
                        this.firstNameErrMsg = globalProxyMgmtErrorService.getErrorFirstName(proxy);
                    }
                    if (this.lastNameErrMsg) {
                        this.lastNameErrMsg = globalProxyMgmtErrorService.getErrorLastName(proxy);
                    }
                    if (this.emailErrMsg) {
                        this.emailErrMsg = globalProxyMgmtErrorService.getErrorEmail(proxy);
                    }
                    if (this.verifyEmailErrMsg) {
                        this.verifyEmailErrMsg = globalProxyMgmtErrorService.getErrorVerifyEmail(proxy);
                    }
                    if (this.relationshipErrMsg) {
                        this.relationshipErrMsg = globalProxyMgmtErrorService.getErrorRelationship(proxy);
                    }
                    if (this.authorizationsErrMsg) {
                        this.authorizationsErrMsg = globalProxyMgmtErrorService.getErrorAuthorizations(proxy);
                    }
                    if (this.startDateErrMsg) {
                        this.startDateErrMsg = globalProxyMgmtErrorService.getErrorDates(proxy)[0];
                    }
                    if (this.stopDateErrMsg) {
                        this.stopDateErrMsg = globalProxyMgmtErrorService.getErrorDates(proxy)[1];
                    }
                }
            }
        }
    }]);
