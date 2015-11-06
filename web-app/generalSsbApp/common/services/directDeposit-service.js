/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositService', ['$resource', function ($resource) {

    var fetchDisclaimer = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'getDisclaimerText'}, {query: {method:'GET', isArray:false}});

    this.getDisclaimer = function () {
        return fetchDisclaimer.query();
    };
    
    var fetchRoles = $resource('../ssb/:controller/:action',
        {controller: 'General', action: 'getRoles'}, {query: {method:'GET', isArray:false}});

    this.getRoles = function () {
        return fetchRoles.query();
    };

}]);