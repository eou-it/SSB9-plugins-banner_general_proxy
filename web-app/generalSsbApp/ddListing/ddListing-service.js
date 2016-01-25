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
    
    this.numAmountsInvalid = 0; // start at one since changed initially
    this.setAmountsValid = function (valid) {
    	if(valid){
    		if(this.numAmountsInvalid > 0){
    			this.numAmountsInvalid--;
    		}
    	}
    	else{
    		this.numAmountsInvalid++;
    	}
    	
    	this.amountsValid = this.numAmountsInvalid <= 0;
    	console.log("num: "+ this.numAmountsInvalid + ", vald: "+ this.amountsValid);
    }

}]);
