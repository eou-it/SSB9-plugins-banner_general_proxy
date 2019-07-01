/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyManagementApp.service('proxyMgmtAppService', ['$rootScope', '$filter', '$resource', function ($rootScope, $filter, $resource) {

    var fetchProxies = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxies'}, {query: {method:'GET', isArray:false}});


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
            {controller: 'ProxyManagement', action: 'createProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.updateProxy = function (entity) {
        console.log(JSON.stringify(entity));
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'updateProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.getProxyStartStopDates = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxyStartStopDates'}).get(params);
    };

}]);
