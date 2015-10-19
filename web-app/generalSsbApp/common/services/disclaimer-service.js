/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('disclaimerService', ['$resource', function ($resource) {

    var fetchDisclaimer = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'getDisclaimerText'}, {query: {method:'GET', isArray:false}});

    this.getDisclaimer = function () {
        return fetchDisclaimer.query();
    };

}]);