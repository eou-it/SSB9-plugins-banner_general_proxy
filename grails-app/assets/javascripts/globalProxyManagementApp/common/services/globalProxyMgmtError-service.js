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

        this.getErrorFirstName = function(proxy) {
            //var msg = 'proxyManagement.message.firstNameRequired';

            //if (!proxy.p_first) {
            //    messages.push({msg: msg, type: 'error'});

            //    return msg;
            //}
            //else {
            //    notificationCenterService.removeNotification(msg);
            //}
        };



       // this.getErrorRelationship = function(proxy) {
       //     var msg = 'proxyManagement.message.relationshipRequired';

       //     if (!proxy.p_retp_code) {
       //         messages.push({msg: msg, type: 'error'});

       //         return msg;
       //     }
       //     else {
       //         notificationCenterService.removeNotification(msg);
       //     }
       // };

       //this.getErrorAuthorizations = function(proxy) {
       //    var msg = 'proxyManagement.message.authorizedPageRequired',
       //        found,
       //        isAtLeastOnePageAuthorized = false;

       //    if (proxy.pages) {
       //        found = _.find(proxy.pages, function (page) {
       //            return page.auth;
       //        });

       //        isAtLeastOnePageAuthorized = !!found;
       //    }

        //    if (!isAtLeastOnePageAuthorized) {
        //        messages.push({msg: msg, type: 'error'});
        //        return msg;
        //    }
        //    else {
        //        notificationCenterService.removeNotification(msg);
        //    }
        //};


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
