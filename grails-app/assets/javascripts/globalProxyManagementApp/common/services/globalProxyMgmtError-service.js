/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.service('globalProxyMgmtErrorService', ['notificationCenterService',
    function (notificationCenterService) {
        let messages = [];
        const proxyProfileMessageCenter = "#proxyProfileErrorMsgCenter";

        this.refreshMessages = function() {
            messages = [];
        };

        this.refreshProxyManagementDateErrorManager = function () {
        };

        this.getErrorBannerId = function (proxy) {
            const invalidTargetMsg = 'globalProxyManagement.message.targetNotValid';
            const invalidBannerIdMsg = 'globalProxyManagement.message.bannerIdRequired';

            if (proxy.isValidBannerId === "false"){
                messages.push({msg: invalidBannerIdMsg, type: 'error'});
                return invalidBannerIdMsg;
            }
            else if (proxy.isValidTarget === "false") {
                messages.push({msg: invalidTargetMsg, type: 'error'});
                return invalidTargetMsg;
            }
            else {
                notificationCenterService.removeNotification(invalidTargetMsg);
                notificationCenterService.removeNotification(invalidBannerIdMsg);
            }
        };

        this.getErrorRelationship = function (proxy) {
            const invalidRelationshipMsg = 'globalProxyManagement.message.relationshipRequired';
            const noPagesAuthorizedMsg = 'proxyManagement.message.noAuthorizationsAvailable';

            if (!proxy.p_retp_code) {
                messages.push({msg: invalidRelationshipMsg, type: 'error'});
                return invalidRelationshipMsg;
            }
            else if(!proxy.pages.length > 0){
                messages.push({msg: noPagesAuthorizedMsg, type: 'error'});
                return noPagesAuthorizedMsg;
            }
            else {
                notificationCenterService.removeNotification(invalidRelationshipMsg);
                notificationCenterService.removeNotification(noPagesAuthorizedMsg);
            }
        };


        this.displayMessages = function() {
            notificationCenterService.setLocalMessageCenter(proxyProfileMessageCenter);

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
