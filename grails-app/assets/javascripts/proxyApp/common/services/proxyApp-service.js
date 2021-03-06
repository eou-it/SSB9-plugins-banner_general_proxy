/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service('proxyAppService', ['$rootScope', '$filter', '$resource', function ($rootScope, $filter, $resource) {

    var fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getConfig'}),
        fetchProxyConfig = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getProxyConfig'}),
        fetchProxyPersonalInfo = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getProxypersonalinformation'}, {query: {method:'GET', isArray:false}}),
        fetchStudentListForProxy = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getStudentListForProxy'}, {query: {method:'GET', isArray:false}});


    var updateProxyHistoryOnPageAccess = function(label) {
        jQuery.ajax({
            url: "proxy/updateProxyHistoryOnPageAccess",
            data: {"label": label},
            async: true
        })};


    //Logs the History for the Proxy Page Access
    this.updateProxyHistoryOnPageAccess = function(label){
        updateProxyHistoryOnPageAccess(label);
    };


    this.config = null;
    this.proxyConfig = null;

    this.getConfiguration = function () {
        // Retrieve configuration just once; don't make a round trip each time it's requested.
        if (!this.config) {
            this.config = fetchConfig.get();
        }

        return this.config;
    };

    this.getProxyConfig = function () {
        // Retrieve configuration just once; don't make a round trip each time it's requested.
        if (!this.proxyConfig) {
            this.proxyConfig = fetchProxyConfig.get();
        }

        return this.proxyConfig;
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


    this.getViewGradesHolds = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getViewGradesHolds'}).get(params);
    };


    this.getGrades = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getGrades'}).get(params);
    };

    this.getTermsForRegistration = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getTermsForRegistration'}).get(params);
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

    this.getAccountSummary = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getAccountSummary'}).get(params);
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
            };
        }
        else {
            return null;
        }
    };

    this.setTerm = function(termObj) {
        sessionStorage.setItem("termCode", termObj.code);
        sessionStorage.setItem("termDesc", termObj.description);
    };

    this.getTerm = function() {
        if (sessionStorage.getItem("termCode")) {
            return {
                code: sessionStorage.getItem("termCode"),
                description: sessionStorage.getItem("termDesc")
            };
        }
        else {
            return null;
        }
    };


    this.checkStudentPageForAccess = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'checkStudentPageForAccess'}).get(params);
    };

    this.getAidYears = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getAidYears'}).query(params);
    };

    this.getTerms = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getTerms'}).query(params);
    };

    this.getCountyList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getCountyList'}).query(params);
    };

    this.getStateList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getStateList'}).query(params);
    };

    this.getNationList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getNationList'}).query(params);
    };

}]);
