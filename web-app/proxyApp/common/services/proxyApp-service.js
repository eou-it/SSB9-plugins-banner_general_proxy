/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service('proxyAppService', ['$rootScope', '$resource', function ($rootScope, $resource) {

    var fetchRoles = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getRoles'}, {query: {method:'GET', isArray:false}}),
        fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getGeneralConfig'}, {query: {method:'GET', isArray:false}}),
        fetchProxyPersonalInfo = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getProxypersonalinformation'}, {query: {method:'GET', isArray:false}}),
        fetchStudentListForProxy = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getStudentListForProxy'}, {query: {method:'GET', isArray:false}});


    this.getRoles = function () {
        return fetchRoles.query();
    };

    this.getGeneralConfig = function () {
        return fetchConfig.query();
    };

    this.getFromPersonalInfo = function (entityName, params) {
        return $resource('../:controller/:action',
            {controller: 'PersonalInformationDetails', action: 'get'+entityName}).get(params);
    };

    this.getProxyPersonalInfo = function () {
        return fetchProxyPersonalInfo.query();
    };

    this.updateProxyPersonalInfo = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'updateProxypersonalinformation'}, {save: {method: 'POST'}}).save(entity);
    };

    this.getStudentListForProxy = function () {
        return fetchStudentListForProxy.query();
    };

    this.getHolds = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getHolds'}).get(params);
    };

    this.getGrades = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getGrades'}).get(params);
    };

    this.getFinancialAidStatus = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getFinancialAidStatus'}).get(params);
    };

    this.getCourseSchedule = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getCourseSchedule'}).get(params);
    };

    this.getDetailSchedule = function(params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getCourseScheduleDetail'}).get(params);
    };

    this.getAwardPackage = function(params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getAwardPackage'}).get(params);
    };

    this.getAwardHistory = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getAwardHistory'}).get(params);
    };

    this.getStudentName = function() {
        return (typeof $rootScope.studentName != "undefined") ? $rootScope.studentName : sessionStorage.getItem("name");
    };

    this.setAidYear = function(aidYearObj) {
        sessionStorage.setItem("aidYearCode", aidYearObj.code);
        sessionStorage.setItem("aidYearDesc", aidYearObj.description);
    };

    this.getAidYear = function() {
        if (sessionStorage.getItem("aidYearCode")) {
            return {
                code: sessionStorage.getItem("aidYearCode"),
                description: sessionStorage.getItem("aidYearDesc")
            }
        }
        else {
            return null;
        }
    };
}]);
