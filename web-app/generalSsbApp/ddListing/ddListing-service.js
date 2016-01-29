/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['$resource', '$filter', '$locale', 'notificationCenterService', function ($resource, $filter, $locale, notificationCenterService) {
    var apListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getApAccountsForCurrentUser'}),
        mostRecentPayrollListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getLastPayDateInfo'}),
        userPayrollAllocationListing = $resource('../ssb/:controller/:action',
            {controller: 'AccountListing', action: 'getUserPayrollAllocations'}),

        /**
         * Compare two accounts to determine if they're actually the same account
         */
        isSameAccount = function(a1, a2) {
            return a1 !== null && a2 !== null &&
                a1.bankAccountNum === a2.bankAccountNum &&
                a1.bankRoutingInfo.bankRoutingNum === a2.bankRoutingInfo.bankRoutingNum &&
                a1.hrIndicator === a2.hrIndicator &&
                a1.apIndicator === a2.apIndicator;
        };

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

    this.checkIfTwoDecimalPlaces = function (num) {
        num = String(num);

        var decSpot = num.indexOf($locale.NUMBER_FORMATS.DECIMAL_SEP),
            result = true;

        if(decSpot >= 0){
            if(num.length - decSpot > 3){
                result = false;
            }
        }

        return result;
    };

    // Validates amount as a valid currency amount and, if not valid, sets notification
    this.validateCurrencyAmountAndSetNotification = function(acct) {
        if(acct.amount > 0){
            if(!this.checkIfTwoDecimalPlaces(acct.amount) || acct.amount > 99999999.99) {
                notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.format.amount'), "error");

                return false;
            }
        }
        else {
            notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.amount.amount'), "error");

            return false;
        }

        return true;
    };

    // Validates percentage and, if not valid, sets notification
    this.validatePercentageAndSetNotification = function(acct) {
        if(acct.percent > 0 && acct.percent <= 100){
            if(!this.checkIfTwoDecimalPlaces(acct.percent)) {
                notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.format.percent'), "error");

                return false;
            }
        }
        else {
            notificationCenterService.displayNotifications($filter('i18n')('directDeposit.invalid.amount.percent'), "error");

            return false;
        }

        return true;
    };

    /**
     * Validate amounts in account on scope and confirm that an account with "Remaining" (aka 100%) does not
     * already exist.  Set appropriate error-related data on scope.
     * @param scope
     * @param acct Account to check
     * @param existingPayrollAccountWithRemainingAmount Payroll account with remaining amount, if exists
     * @returns {boolean} True if everything validates.
     */
    this.validateAmountsForAccount = function (scope, acct, existingPayrollAccountWithRemainingAmount) {
        var self = this,
            amountErr = null,

            validateNotMoreThanOneRemainingAccount = function() {
                if(existingPayrollAccountWithRemainingAmount &&
                    !isSameAccount(scope.account, existingPayrollAccountWithRemainingAmount)) {

                    scope.amountMessage = $filter('i18n')('directDeposit.invalid.amount.remaining');
                    amountErr = 'rem';
                    notificationCenterService.displayNotifications(scope.amountMessage, "error");
                }
            };

        if(acct.amountType === 'amount') {
            if (!self.validateCurrencyAmountAndSetNotification(acct)) {
                amountErr = 'amt';
            }
        }
        else if(acct.amountType === 'remaining') {
            validateNotMoreThanOneRemainingAccount();
        }
        else if(acct.amountType === 'percentage') {
            if (!self.validatePercentageAndSetNotification(acct)) {
                amountErr = 'pct';
            }
            // A percentage of 100% is the same as "Remaining" from a business rules perspective.
            // Note that the type-converting equality comparison (== as opposed to the strict ===) for
            // percent is necessary as the value is a string, and it is being compared with a number.
            else if (acct.percent == 100) {
                validateNotMoreThanOneRemainingAccount();
            }
        }

        if (amountErr) {
            scope.amountErr = amountErr;
        }

        return !amountErr;
    };

    this.validateAllAmounts = function (accounts){
        var self = this;

        _.each(accounts, function(acct) {
            if(acct.amountType === 'amount') {
                if (!self.validateCurrencyAmountAndSetNotification(acct)) {
                    return false;
                }
            }
            else if(acct.amountType === 'percentage') {
                if (!self.validatePercentageAndSetNotification(acct)) {
                    return false;
                }
            }
        });

        return true;
    };

}]);
