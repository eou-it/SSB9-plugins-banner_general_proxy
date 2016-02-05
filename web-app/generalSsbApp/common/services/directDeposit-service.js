/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositService', ['$resource', function ($resource) {

    var fetchRoles = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getRoles'}, {query: {method:'GET', isArray:false}}),
        fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'DirectDepositConfiguration', action: 'getConfig'}),
        fetchCurrencySymbol = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getCurrency'});


    // CONSTANTS
    this.REMAINING_NONE = 0;
    this.REMAINING_ONE = 1;
    this.REMAINING_MULTIPLE = 2;


    this.getRoles = function () {
        return fetchRoles.query();
    };

    this.config = null;

    this.getConfiguration = function () {
        // Retrieve configuration just once; don't make a round trip each time it's requested.
        if (!this.config) {
            this.config = fetchConfig.get();
        }

        return this.config;
    };

    this.getCurrencySymbol = function () {
        return fetchCurrencySymbol.get();
    }

    // Destroy all popovers (i.e. Bootstrap popovers)
    this.destroyAllPopovers = function (){
        // When created, the actual popover is the next sibling adjacent to the
        // AngularJS popover element.  The actual popover has the '.popover.in'
        // CSS selector.  Here's a diagram:
        //
        //     ANGULARJS ELEMENT        ACTUAL POPOVER VISIBLE TO USER
        //     <pop-over ...></pop-over><div class="popover in" ...> ... </div>
        //
        // Thus the previous sibling (grabbed with prev()) is the
        // AngularJS popover element that needs to have 'destroy' called on it.
        $('body').find('.popover.in').prev().popover('destroy');
    };

    /**
     * Does this account have an amount of "Remaining"?
     * @param account
     * @returns {boolean}
     */
    this.isRemaining = function(account) {
        var amountType = account.amountType;

        // CASE 1: Allocation returned from ddListingService function getUserPayrollAllocationListing in this way.
        // CASE 2: Self-explanatory
        // CASE 3: A percentage of 100% is the same as "Remaining" from a business rules perspective.
        // Note that the type-converting equality comparison (== as opposed to the strict ===) for
        // percent is necessary as the value is a string, and it is being compared with a number.
        return !amountType && account.allocation === "100%" ||          // CASE 1
               amountType === 'remaining' ||                            // CASE 2
               (amountType === 'percentage' && account.percent == 100); // CASE 3
    };

}]);