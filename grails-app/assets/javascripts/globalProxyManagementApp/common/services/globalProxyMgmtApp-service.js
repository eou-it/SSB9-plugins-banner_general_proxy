/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

globalProxyManagementApp.service('globalProxyMgmtAppService', ['$rootScope', '$filter', '$resource', '$q', 'notificationCenterService', function ($rootScope, $filter, $resource, $q, notificationCenterService) {

    var fetchProxies = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getGlobalProxies'}, {query: {method:'GET', isArray:false}}),
        fetchDoesUserHaveActivePreferredEmail = $resource('../ssb/:controller/:action',
                    {controller: 'GlobalProxy', action: 'getLoggedInUserHasActivePreferredEmail'}, {query: {method:'GET', isArray:false}}),
        fetchRelationshipOptions = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getRelationshipOptions'}, {query: {method:'GET', isArray:false}}),
        fetchCommunicationLog = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getCommunicationLog'}, {query: {method:'GET', isArray:true}}),
        fetchAuthLog = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getHistoryLog'}, {query: {method:'GET', isArray:false}}),
        fetchGlobalProxiesByQuery = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getGlobalProxies'}, {query: {method:'GET', isArray:false}}),
        checkIfGlobalProxyAccessTargetIsValid = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'checkIfGlobalProxyAccessTargetIsValid'}, {query: {method:'GET', isArray:false}});

    this.getProxyList = function () {
        return fetchProxies.query();
    };

    this.getProxy = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxy'}).get(params);
    };

    this.deleteProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'deleteProxy'}, {delete: {method:'POST'}}).delete(entity);
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
            {controller: 'GlobalProxy', action: 'getDataModelOnRelationshipChange'}).get(params);
    };

    this.getRelationshipOptions = function () {
        return fetchRelationshipOptions.query();
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

    this.fetchGlobalProxiesByQuery = function (params){
        return fetchGlobalProxiesByQuery.query(params)
    };

    this.getDoesUserHaveActivePreferredEmailAddress = function(params){
        return fetchDoesUserHaveActivePreferredEmail.query(params)
    };

    this.isGlobalProxyAccessTargetValid = function(params){
        return checkIfGlobalProxyAccessTargetIsValid.query(params);
    }

}]);
