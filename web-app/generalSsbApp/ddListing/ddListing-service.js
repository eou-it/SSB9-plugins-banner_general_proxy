/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['$resource', function ($resource) {
    var listing = $resource('../ssb/:controller/:action',
        {controller: 'AccountListing', action: 'getMyAccounts'});

    this.getDirectDepositListing = function (){
        return listing.query();
    }

    this.getPayrollListing = function() {
        //STUB
    };
}]);
