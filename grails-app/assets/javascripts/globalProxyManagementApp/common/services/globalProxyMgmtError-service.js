/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.service('globalProxyMgmtErrorService', ['notificationCenterService', '$filter',
    function (notificationCenterService, $filter) {
        var messages = [],
            proxyProfileMessageCenter = "#proxyProfileErrorMsgCenter";

        this.refreshMessages = function() {
            messages = [];
        };

        this.refreshProxyManagementDateErrorManager = function () {
        };

        this.getErrorBannerId = function (proxy) {
            var invalidTargetMsg = 'globalProxyManagement.message.targetNotValid';
            var invalidBannerIdMsg = 'globalProxyManagement.message.bannerIdRequired';


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
            var invalidRelationshipMsg = 'globalProxyManagement.message.relationshipRequired';

            if (!proxy.p_retp_code) {
                messages.push({msg: invalidRelationshipMsg, type: 'error'});
                return invalidRelationshipMsg;
            } else {
                notificationCenterService.removeNotification(invalidRelationshipMsg);
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
