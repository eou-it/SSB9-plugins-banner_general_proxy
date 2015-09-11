/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositListingService', ['$resource', function ($resource) {
    var listing = $resource('../ssb/:controller/:action',
        {controller: 'AccountListing', action: 'getMyAccounts'});

    this.getDirectDepositListing = function (){
        return listing.query();
    }
}]);
