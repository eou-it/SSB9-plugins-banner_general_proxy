/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyManagementApp.service('proxyMgmtAppService', ['$rootScope', '$filter', '$resource', '$q', 'notificationCenterService', function ($rootScope, $filter, $resource, $q, notificationCenterService) {

    var fetchProxies = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxies'}, {query: {method:'GET', isArray:false}}),
        fetchRelationshipOptions = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getRelationshipOptions'}, {query: {method:'GET', isArray:false}}),
        fetchCommunicationLog = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getCommunicationLog'}, {query: {method:'GET', isArray:true}});


    this.getProxyList = function () {
        return fetchProxies.query();
    };

    this.getProxy = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxy'}).get(params);
    };

    this.deleteProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'deleteProxy'}, {delete: {method:'POST'}}).delete(entity);
    };

    this.createProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'createUpdateProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.updateProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'updateProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.getDataModelOnRelationshipChange = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getDataModelOnRelationshipChange'}).get(params);
    };

    this.getRelationshipOptions = function () {
        return fetchRelationshipOptions.query();
    };

    this.resetProxyPassword = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'resetProxyPassword'}).get(params);
    };

    this.emailAuthentications = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'emailAuthentications'}).get(params);
    };

    this.emailPassphrase = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'emailPassphrase'}).get(params);
    };

    this.getClonedProxiesList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxiesList'}).get(params);
    };

    this.getClonedProxiesListOnCreate = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxiesListOnCreate'}).get(params);
    };

    this.getClonedAuthorizationsList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getDataModelOnAuthorizationChange'}).get(params);
    };

    this.getAddProxiesList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxyAddList'}).get(params);
    };

    var sendCommunicationLog = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'sendCommunicationLog'}).get(params);
    };

    this.getFromPersonalInfo = function (entityName, params) {
        return $resource('../:controller/:action',
            {controller: 'ProxyManagement', action: 'get'+entityName}).get(params);
    };

    this.getCommunicationLog = function (params) {
        var deferred = $q.defer(),
            logEntries = fetchCommunicationLog.query(params),
            result;

        logEntries.$promise.then(function (response) {
            _.each(response, function (logEntry) {
                logEntry.resend.resendEmail = function () {
                    var self = this;

                    sendCommunicationLog({id: self.rowid}).$promise.then(function (response) {
                        var messageType, message;

                        if (response.failure) {
                            messageType = 'error';
                            message = response.message;
                        } else {
                            if (response.resendStatus == 'ERROR') {
                                messageType = 'error';
                                message = 'proxyManagement.message.resendCommunicationLogFailure';
                            } else {
                                messageType = 'success';
                                message = 'proxyManagement.message.resendCommunicationLogSuccess';
                            }
                        }
                        notificationCenterService.clearNotifications();
                        notificationCenterService.addNotification(message, messageType, true);
                    });
                };

            });

            result = {result: response, length: response.length};

            deferred.resolve(result);
        });

        return deferred;
    };

}]);
