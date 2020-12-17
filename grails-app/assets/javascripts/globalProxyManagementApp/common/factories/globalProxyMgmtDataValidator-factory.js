/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.factory('GlobalProxyManagementDataValidator', ['globalProxyMgmtErrorService',
    function (globalProxyMgmtErrorService) {
        return function () {
            return {
                bannerIdErrMsg: '',
                relationshipErrMsg: '',
                authorizationsErrMsg: '',
                isValidProxyData: function(proxy, isSubmit){
                    globalProxyMgmtErrorService.refreshMessages();
                    this.bannerIdErrMsg = (!isSubmit && !proxy.targetId) ? '' : globalProxyMgmtErrorService.getErrorBannerId(proxy);
                    this.relationshipErrMsg = (!isSubmit && !proxy.p_retp_code) ? '' : globalProxyMgmtErrorService.getErrorRelationship(proxy);
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
