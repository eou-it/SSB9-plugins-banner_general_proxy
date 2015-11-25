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
        return userPayrollAllocationListing.get();
    };

}]);

generalSsbApp.service('ddListingInitService', ['ddListingService', function (ddListingService) {
	
	var account;
	ddListingService.getApListing().$promise.then(
        function (response) {
            // By default, set A/P account as currently active account, as it can be edited inline (in desktop
            // view), while payroll accounts can not be.
            account = getApAccountFromResponse(response);
    });
	
	var getApAccountFromResponse = function(response) {
        var account = null;

        if (response.length) {
            // Probably only one account has been returned, but if more than one, return the one with the
            // highest priority (i.e. lowest integer value).
            _.each(response, function(acctFromResponse) {
                if (account === null || acctFromResponse.priority < account.priority) {
                    account = acctFromResponse;
                }
            });

        }

        return account;
    }
	

	return account;

}]);
