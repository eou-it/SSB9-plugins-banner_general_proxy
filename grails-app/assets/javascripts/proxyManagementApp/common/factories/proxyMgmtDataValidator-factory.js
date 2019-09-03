/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyManagementApp.factory('ProxyManagementDataValidator', ['proxyMgmtErrorService',
    function (proxyMgmtErrorService) {
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
                    var errors = proxyMgmtErrorService.getErrorDates(proxy, isSubmit);
                    this.startDateErrMsg = errors[0];
                    this.stopDateErrMsg = errors[1];
                },

                isValidProxyData: function(proxy, isUpdate){
                    proxyMgmtErrorService.refreshMessages();
                    if (isUpdate) {
                        // The corresponding fields for these errors are always empty for an update, so shim the error messages.
                        this.firstNameErrMsg = false;
                        this.lastNameErrMsg = false;
                        this.emailErrMsg = false;
                        this.verifyEmailErrMsg = false;
                    } else {
                        this.firstNameErrMsg = proxyMgmtErrorService.getErrorFirstName(proxy);
                        this.lastNameErrMsg = proxyMgmtErrorService.getErrorLastName(proxy);
                        this.emailErrMsg = proxyMgmtErrorService.getErrorEmail(proxy);
                        this.verifyEmailErrMsg = proxyMgmtErrorService.getErrorVerifyEmail(proxy);
                    }

                    this.relationshipErrMsg = proxyMgmtErrorService.getErrorRelationship(proxy);
                    this.authorizationsErrMsg = proxyMgmtErrorService.getErrorAuthorizations(proxy);
                    this.setStartAndStopDateErrors(proxy, true);

                    return !(this.firstNameErrMsg || this.lastNameErrMsg || this.emailErrMsg || this.verifyEmailErrMsg ||
                        this.relationshipErrMsg || this.authorizationsErrMsg || this.startDateErrMsg || this.stopDateErrMsg);
                },

                removeProxyProfileFieldErrors: function (proxy) {
                    if (this.firstNameErrMsg) {
                        this.firstNameErrMsg = proxyMgmtErrorService.getErrorFirstName(proxy);
                    }
                    if (this.lastNameErrMsg) {
                        this.lastNameErrMsg = proxyMgmtErrorService.getErrorLastName(proxy);
                    }
                    if (this.emailErrMsg) {
                        this.emailErrMsg = proxyMgmtErrorService.getErrorEmail(proxy);
                    }
                    if (this.verifyEmailErrMsg) {
                        this.verifyEmailErrMsg = proxyMgmtErrorService.getErrorVerifyEmail(proxy);
                    }
                    if (this.relationshipErrMsg) {
                        this.relationshipErrMsg = proxyMgmtErrorService.getErrorRelationship(proxy);
                    }
                    if (this.authorizationsErrMsg) {
                        this.authorizationsErrMsg = proxyMgmtErrorService.getErrorAuthorizations(proxy);
                    }
                    if (this.startDateErrMsg) {
                        this.startDateErrMsg = proxyMgmtErrorService.getErrorDates(proxy)[0];
                    }
                    if (this.stopDateErrMsg) {
                        this.stopDateErrMsg = proxyMgmtErrorService.getErrorDates(proxy)[1];
                    }
                }
            }
        }
    }]);
