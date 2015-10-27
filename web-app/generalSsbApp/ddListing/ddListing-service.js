/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['$resource', function ($resource) {
    var listing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getMyAccounts'}),
        mostRecentPayrollListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getLastPayDateInfo'});

    this.getDirectDepositListing = function (){
        return listing.query();
    };

    this.getMostRecentPayrollListing = function() {
        return mostRecentPayrollListing.get();
    };

    this.getUserAllocationAmount = function () {
        // STUB
        // TODO: choose between percentage or amount designated by user (latter could be
        // remaining amount)
        // Does this belong here or in controller or in directive?

        return 'Remaining'; // $500.00, 70%, or Remaining
    };

    this.getDistributionAmount = function () {
        // STUB
        // TODO: calculate amount to be distributed (displayed in lower right corner of proposed allocation),
        // presumably based on actual pay and either percentage or amount designated by user (latter could be
        // remaining amount).
        // Does this belong here or in controller or in directive?
        return 747;
    };

}]);
