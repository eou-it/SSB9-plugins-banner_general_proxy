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
        return null; //TODO: TEMPORARY PLACEHOLDER
    };

    this.deleteProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'deleteProxy'}, {delete: {method:'POST'}}).delete(entity);
    };

    this.getProxyStartStopDates = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxyStartStopDates'}).get(params);
    };

}]);
