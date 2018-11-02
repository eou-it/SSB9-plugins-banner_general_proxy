/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service('proxyAppService', ['$rootScope', '$filter', '$resource', function ($rootScope, $filter, $resource) {

    var fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getConfig'}),
        fetchProxyPersonalInfo = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getProxypersonalinformation'}, {query: {method:'GET', isArray:false}}),
        fetchStudentListForProxy = $resource('../ssb/:controller/:action',
            {controller: 'Proxy', action: 'getStudentListForProxy'}, {query: {method:'GET', isArray:false}});

       var dateFmt,
           calendar = (function(){
                   var locale = $('meta[name=locale]').attr("content");

                        if(locale.split('-')[0] === 'ar') {
                           dateFmt = $filter('i18n')('default.date.format');
                            return $.calendars.instance('islamic');
                        }
                   else {
                            dateFmt = $filter('i18n')('default.date.format').toLowerCase();
                            return $.calendars.instance();
                        }
                }());
       

       this.stringToDate = function (date) {
               var result;
               try {
                   result = calendar.parseDate(dateFmt, date).toJSDate();
                       return result;
                   }
               catch (exception) {
                       return null;}
           };


    this.config = null;

    this.getConfiguration = function () {
        // Retrieve configuration just once; don't make a round trip each time it's requested.
        if (!this.config) {
            this.config = fetchConfig.get();
        }

        return this.config;
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

    this.setTerm = function(termrObj) {
        sessionStorage.setItem("termCode", termrObj.code);
        sessionStorage.setItem("termDesc", termrObj.description);
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

}]);
