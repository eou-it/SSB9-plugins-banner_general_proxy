/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

globalProxyManagementApp.service('globalProxyMgmtAppService', ['$rootScope', '$filter', '$resource', '$q', function ($rootScope, $filter, $resource, $q) {

    const fetchProxies = $resource('../ssb/:controller/:action',
        {controller: 'GlobalProxy', action: 'getGlobalProxies'}, {query: {method: 'GET', isArray: false}}),
        fetchDoesUserHaveValidEmailAddress = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getLoggedInUserHasValidEmailAddress'}, {query: {method: 'GET', isArray: false}}),
        fetchRelationshipOptions = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getRelationshipOptions'}, {query: {method: 'GET', isArray: false}}),
        fetchGlobalProxiesByQuery = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getGlobalProxies'}, {query: {method: 'GET', isArray: false}}),
        checkIfGlobalProxyAccessTargetIsValid = $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'checkIfGlobalProxyAccessTargetIsValid'}, {query: {method: 'GET', isArray: false}});

    this.getProxyList = function () {
        return fetchProxies.query();
    };

    this.deleteProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'deleteProxy'}, {delete: {method:'POST'}}).delete(entity);
    };

    this.createProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'createGlobalProxyRelationship'}, {delete: {method:'POST'}}).save(entity);
    };

    this.getDataModelOnRelationshipChange = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'GlobalProxy', action: 'getDataModelOnRelationshipChange'}).get(params);
    };

    this.getRelationshipOptions = function () {
        return fetchRelationshipOptions.query();
    };

    this.fetchGlobalProxiesByQuery = function (params){
        return fetchGlobalProxiesByQuery.query(params)
    };

    this.getDoesUserHaveValidEmailAddress = function(params){
        return fetchDoesUserHaveValidEmailAddress.query(params)
    };

    this.isGlobalProxyAccessTargetValid = function(params){
        return checkIfGlobalProxyAccessTargetIsValid.query(params);
    }

}]);
