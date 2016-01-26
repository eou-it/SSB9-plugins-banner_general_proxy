/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['$resource', '$filter', 'notificationCenterService', function ($resource, $filter, notificationCenterService) {
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
    
    // flag to indicate if the listing controller is initialized
    this.init = false;

    // return the state of the listing controller and set the state to initialized
    this.isInit = function(){
        var ret = !!this.init;
        this.init = true;
        return ret;
    };

    // set init to false to force the listing controller to reload
    this.doReload = function(){
        this.init = false;
    };
    
    // flag to indicate if all amounts are valid for save 
    this.amountsValid = true;
    
    this.numAmountsInvalid = 0;
    this.setAmountsValid = function (valid) {
        if(valid){
            this.numAmountsInvalid--;
        }
        else{
            this.numAmountsInvalid++;
        }
        
        this.amountsValid = this.numAmountsInvalid <= 0;
    };
    
    this.validateAllAmounts = function (accounts){
        var result = true,
            i;
        
        for(i = 0; i < accounts.length; i++) {
            var isValid = true;
            var acct = accounts[i];

            if(acct.amountType === 'amount') {
                if(acct.amount <= 0){
                    notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.amount.amount'), "error");

                    isValid = false;
                }
            }
            else if(acct.amountType === 'percentage') {
                if(acct.percent <= 0 || acct.percent > 100){
                    notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.amount.percent'), "error");

                    isValid = false;
                }
            }

            result = result && isValid;
        }
        
        return result;
    };

}]);
