/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service('proxyAppService', ['$rootScope', '$resource', function ($rootScope, $resource) {

    var fetchRoles = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getRoles'}, {query: {method:'GET', isArray:false}}),
        fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getGeneralConfig'}, {query: {method:'GET', isArray:false}}),
        fetchProxyPersonalInfo = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getProxypersonalinformation'}, {query: {method:'GET', isArray:false}});


    this.getRoles = function () {
        return fetchRoles.query();
    };

    this.getGeneralConfig = function () {
        return fetchConfig.query();
    };

    this.getFromPersonalInfo = function (entityName, params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'PersonalInformationDetails', action: 'get'+entityName}).get(params);
    };

    this.getProxyPersonalInfo = function () {
        return fetchProxyPersonalInfo.query();
    };

    this.updateProxyPersonalInfo = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'updateProxypersonalinformation'}, {save: {method: 'POST'}}).save(entity);
    };

}]);