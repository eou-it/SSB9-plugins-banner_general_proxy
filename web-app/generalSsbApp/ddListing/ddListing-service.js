/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['$resource', function ($resource) {
    var apListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getApAccountsForCurrentUser'}),
        mostRecentPayrollListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getLastPayDateInfo'}),
        userPayrollAllocationListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getUserPayrollAllocations'});

    this.getApListing = function (){
        return apListing.query();
    };

    this.getMostRecentPayrollListing = function() {
        return mostRecentPayrollListing.get();
    };

    this.getUserPayrollAllocationListing = function() {
        return userPayrollAllocationListing.query();
    };

}]);
