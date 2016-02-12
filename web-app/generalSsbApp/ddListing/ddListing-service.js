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

    // Scope for main listing controller, which contains all allocations (as opposed to *child* listing
    // controllers created by, for example, directive payAccountInfoProposedDesktop, which do not contain
    // all allocations).
    this.mainListingControllerScope = null;

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
            msg = $filter('i18n')('directDeposit.invalid.format.amount');
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
            msg = $filter('i18n')('directDeposit.invalid.amount.percent');
            notificationCenterService.displayNotifications(msg, "error");
        }

        return msg;
    };

    /**
     * Determine if more than one account with "Remaining" amount exists.
     * @param acct The account being checked
     * @returns {boolean} True if more than one "Remaining" account exists
     */
    this.accountWithRemainingAmountAlreadyExists = function(acct) {
        var self = this,
            mainListScope = self.mainListingControllerScope,
            allocations,
            found;

        if (!mainListScope.hasPayAccountsProposed) {
            return false;
        }

        allocations = mainListScope.distributions.proposed.allocations;

        found = _.find(allocations, function(alloc) {
            return directDepositService.isRemaining(alloc) && !isSameAccount(acct, alloc)
        });

        return !!found;
    };

    this.hasMultipleRemainingAmountAllocations = function() {
        var self = this;

        return self.mainListingControllerScope.hasMultipleRemainingAmountAllocations();
    };

    this.accountWithRemainingAmountAlreadyExistsAndSetNotification = function(acct) {
        var self = this,
            msg = null;

        if(self.accountWithRemainingAmountAlreadyExists(acct)) {
            msg = $filter('i18n')('directDeposit.invalid.amount.remaining');
            notificationCenterService.displayNotifications(msg, "error");
        }

        return msg;
    };

    /**
     * Validate amounts in account on scope and confirm that an account with "Remaining" (aka 100%) does not
     * already exist.  Set appropriate error-related data on scope.
     * @param scope Scope on which to set error-related data
     * @param acct Account to check
     * @returns {boolean} True if everything validates
     */
    this.validateAmountForAccount = function (scope, acct) {
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
            msg = self.accountWithRemainingAmountAlreadyExistsAndSetNotification(acct);
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

    this.validateAmountsForAllAccountsAndSetNotification = function(accounts) {
        var self = this,
            invalidAcct,
            isInvalidRemainingPositionAndSetNotification = function(alloc) {
                if (!directDepositService.isLastPriority(alloc, accounts)) {
                    // It's not in the last position, as is required
                    var msg = 'directDeposit.invalid.amount.remaining.placement';
                    notificationCenterService.displayNotifications(msg, "error");

                    return true;
                }

                return false;
            };

        invalidAcct = _.find(accounts, function(acct) {
            if(acct.amountType === 'amount') {
                return self.isInvalidCurrencyAmountAndSetNotification(acct);
            }
            else if(directDepositService.isRemaining(acct)) {
                return self.accountWithRemainingAmountAlreadyExistsAndSetNotification(acct) ||
                       isInvalidRemainingPositionAndSetNotification(acct);
            }
            else if(acct.amountType === 'percentage') {
                return self.isInvalidPercentageAndSetNotification(acct);
            }
            else {
                return true;
            }
        });

        return !invalidAcct;
    };

    this.updateWhetherHasPayrollRemainingAmount = function() {
        var self = this;

        self.mainListingControllerScope.updateWhetherHasPayrollRemainingAmount();
    };

    this.calculateAmountsBasedOnPayHistory = function() {
        var self =this;

        self.mainListingControllerScope.calculateAmountsBasedOnPayHistory();
    };

}]);
