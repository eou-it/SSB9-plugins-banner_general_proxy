/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddListingService', ['directDepositService', '$resource', '$filter', 'notificationCenterService',
    function (directDepositService, $resource, $filter, notificationCenterService) {
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

        var decSpot = num.indexOf('.'),
            result = true;

        if(decSpot >= 0){
            if(num.length - decSpot > 3){
                result = false;
            }
        }

        return result;
    };

    // Validates amount as a valid currency amount and, if not valid, sets notification
    this.isInvalidCurrencyAmountAndSetNotification = function(acct) {
        var msg = false;
        
        if(acct.amount > 0){
            if(!this.checkIfTwoDecimalPlaces(acct.amount) || acct.amount > 99999999.99) {
                msg = $filter('i18n')('directDeposit.invalid.format.amount');
                notificationCenterService.displayNotifications(msg, "error");
            }
        }
        else {
            msg = $filter('i18n')('directDeposit.invalid.amount.amount');
            notificationCenterService.displayNotifications(msg, "error");
        }

        return msg;
    };

    // Validates percentage and, if not valid, sets notification
    this.isInvalidPercentageAndSetNotification = function(acct) {
        var msg = false;
        
        if(acct.percent > 0 && acct.percent <= 100){
            if(!this.checkIfTwoDecimalPlaces(acct.percent)) {
                msg = $filter('i18n')('directDeposit.invalid.format.percent');
                notificationCenterService.displayNotifications(msg, "error");
            }
        }
        else {
            msg = $filter('i18n')('directDeposit.invalid.amount.percent')
            notificationCenterService.displayNotifications(msg, "error");
        }

        return msg;
    };

    /**
     * Determine if a remaining account already exists.
     * If one already exists and the account that's being checked is the same account as that, return false.
     * @param acct The account being checked
     * @param existingPayrollAccountWithRemainingAmount Existing payroll account with remaining account, if any
     * @returns {boolean} True if a different "Remaining" account already exists
     */
    this.accountWithRemainingAmountAlreadyExists = function(acct, existingPayrollAccountWithRemainingAmount) {
        return existingPayrollAccountWithRemainingAmount &&
               !isSameAccount(acct, existingPayrollAccountWithRemainingAmount);
    };

    this.hasMoreThanOneRemainingAccount = function(acct, existingPayrollAccountWithRemainingAmount) {
        var self = this,
            msg = false;

        if(self.accountWithRemainingAmountAlreadyExists(acct, existingPayrollAccountWithRemainingAmount)) {
            msg = $filter('i18n')('directDeposit.invalid.amount.remaining');
            notificationCenterService.displayNotifications(msg, "error");
        }

        return msg;
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
            msg = false;

        if(acct.amountType === 'amount') {
            msg = self.isInvalidCurrencyAmountAndSetNotification(acct);
            if (msg) {
                amountErr = 'amt';
            }
        }
        else if(directDepositService.isRemaining(acct)) {
            msg = self.hasMoreThanOneRemainingAccount(acct, existingPayrollAccountWithRemainingAmount);
            if (msg) {
                amountErr = 'rem';
            }
        }
        else if(acct.amountType === 'percentage') {
            msg = self.isInvalidPercentageAndSetNotification(acct);
            if (msg) {
                amountErr = 'pct';
            }
        }
        else {
            amountErr = true;
        }

        if (amountErr) {
            scope.amountErr = amountErr;
            scope.amountMessage = msg;
        }

        return !amountErr;
    };

    this.validateAmountsForAllAccountsAndSetNotification = function (accounts, existingPayrollAccountWithRemainingAmount){
        var self = this,
            result = true;

        _.each(accounts, function(acct) {
            if(acct.amountType === 'amount') {
                if (self.isInvalidCurrencyAmountAndSetNotification(acct)) {
                    result = false;
                    return;
                }
            }
            else if(directDepositService.isRemaining(acct)) {
                if (self.hasMoreThanOneRemainingAccount(acct, existingPayrollAccountWithRemainingAmount)) {
                    result = false;
                    return;
                }
            }
            else if(acct.amountType === 'percentage') {
                if (self.isInvalidPercentageAndSetNotification(acct)) {
                    result = false;
                    return;
                }
            }
            else {
                result = false;
                return;
            }
        });

        return result;
    };

}]);
