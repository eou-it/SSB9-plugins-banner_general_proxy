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
        // A percentage of 100% is the same as "Remaining" from a business rules perspective.
        // Note that the type-converting equality comparison (== as opposed to the strict ===) for
        // percent is necessary as the value is a string, and it is being compared with a number.
        return (account.amountType === 'remaining' || (account.amountType === 'percentage' && account.percent == 100));
    };

}]);