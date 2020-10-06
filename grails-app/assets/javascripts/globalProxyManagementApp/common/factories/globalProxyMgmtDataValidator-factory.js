/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.factory('GlobalProxyManagementDataValidator', ['globalProxyMgmtErrorService',
    function (globalProxyMgmtErrorService) {
        return function () {
            return {
                bannerIdErrMsg: '',
                relationshipErrMsg: '',
                authorizationsErrMsg: '',
                isValidProxyData: function(proxy){
                    globalProxyMgmtErrorService.refreshMessages();
                    this.bannerIdErrMsg = globalProxyMgmtErrorService.getErrorBannerId(proxy);
                    this.relationshipErrMsg = globalProxyMgmtErrorService.getErrorRelationship(proxy);
                    return !(this.bannerIdErrMsg || this.relationshipErrMsg);
                },

                removeProxyProfileFieldErrors: function (proxy) {
                    if (this.bannerIdErrMsg){
                        this.bannerIdErrMsg = globalProxyMgmtErrorService.getErrorBannerId(proxy);
                    }
                    if (this.relationshipErrMsg) {
                        this.relationshipErrMsg = globalProxyMgmtErrorService.getErrorRelationship(proxy);
                    }
                    if (this.authorizationsErrMsg) {
                        this.authorizationsErrMsg = globalProxyMgmtErrorService.getErrorAuthorizations(proxy);
                    }
                }
            }
        }
    }]);
