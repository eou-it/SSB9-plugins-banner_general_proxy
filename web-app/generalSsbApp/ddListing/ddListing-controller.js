/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$state', '$modal', '$filter',
    'ddListingService', 'ddEditAccountService', 'directDepositService', 'notificationCenterService',
    function ($scope, $state, $modal, $filter, ddListingService, ddEditAccountService, directDepositService,
              notificationCenterService){

        // LOCAL FUNCTIONS
        // ---------------
        /**
         * Select the one AP account that will be displayed to user, according to business rules.
         */
        this.getApAccountFromResponse = function(response) {
            var account = null;

            if (response.length) {
                // Probably only one account has been returned, but if more than one, return the one with the
                // highest priority (i.e. lowest integer value).
                _.each(response, function(acctFromResponse) {
                    if (account === null || acctFromResponse.priority < account.priority) {
                        account = acctFromResponse;
                    }
                });

            }

            return account;
        };

        /**
         * Initialize controller
         */
        this.init = function() {

            var self = this;
            
            // if the listing controller has already been initialized, then abort
            if(ddListingService.isInit()) return;

            ddEditAccountService.setSyncedAccounts(false);

            ddListingService.getApListing().$promise.then(
                function (response) {
                    // By default, set A/P account as currently active account, as it can be edited inline (in desktop
                    // view), while payroll accounts can not be.
                    $scope.account = self.getApAccountFromResponse(response);
                    $scope.hasApAccount = !!$scope.account;
                    $scope.accountLoaded = true;

                    self.syncAccounts();
            });

            $scope.distributions = {
                mostRecent: null,
                proposed: null
            };

            ddListingService.getMostRecentPayrollListing().$promise.then( function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    $scope.distributions.mostRecent = response;
                    $scope.hasPayAccountsMostRecent = !!response.docAccts
                    $scope.payAccountsMostRecentLoaded = true;

                    self.syncAccounts();
                }
            });

            ddListingService.getUserPayrollAllocationListing().$promise.then( function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    $scope.distributions.proposed = response;
                    $scope.hasPayAccountsProposed = !!response.allocations.length
                    $scope.payAccountsProposedLoaded = true;

                    self.syncAccounts();

                    $scope.updateWhetherHasMaxPayrollAccounts();
                }
            });
        };
        
        // if an account is used for AP and Payroll, have scope.account and allocation[x] point to same
        // account object so that they are always in sync in the UI. The backend will save the changes
        // to both records if it needs to.
        this.isSynced = false;
        this.syncAccounts = function () {

            if(!this.isSynced){
                if(!$scope.isEmployee){
                    //should be no need to sync pure student, only has an AP account
                    this.isSynced = true;
                }
                else {
                    // make sure AP and Payroll accounts are loaded before trying to sync them
                    if($scope.payAccountsProposedLoaded && $scope.hasPayAccountsProposed && $scope.accountLoaded && $scope.hasApAccount) {
    
                        var allocs = $scope.distributions.proposed.allocations, i;
                        for(i = 0; i < allocs.length; i++) {
                            if(allocs[i].bankRoutingInfo.bankRoutingNum === $scope.account.bankRoutingInfo.bankRoutingNum 
                                    && allocs[i].bankAccountNum === $scope.account.bankAccountNum){

                                $scope.account = allocs[i];
                                ddEditAccountService.setSyncedAccounts($scope.account.bankAccountNum);
                            }
                        }
                        this.isSynced = true;
                    }
                }
            }
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.payAccountsMostRecentLoaded = false;
        $scope.payAccountsProposedLoaded = false;
        $scope.hasPayAccountsMostRecent = false;
        $scope.hasPayAccountsProposed = false;
        $scope.payPanelMostRecentCollapsed = false;
        $scope.payPanelProposedCollapsed = false;

        $scope.distributions = null;
        $scope.account = null; // Currently active account.  Could be A/P or payroll.
        $scope.accountLoaded = false;
        $scope.hasApAccount = false;
        $scope.panelCollapsed = false;
        $scope.authorizedChanges = false;
        $scope.apAccountSelectedForDelete = false;
        $scope.hasMaxPayrollAccounts = false;

        // CONTROLLER FUNCTIONS
        // --------------------

        // Payroll
        $scope.dateStringForPayDistHeader = function () {
            var date = $scope.distributions.mostRecent.payDate;

            return date ? ' as of ' + date : '';
        };

        $scope.totalPayAmounts = function (distribution) {
            var total = 0;

            _.each(distribution.allocations, function(allocation) {
                total += allocation.amount;
            });

            return total;
        };

        // Accounts Payable
        $scope.apListingColumns = [
            { tabindex: '0', title: $filter('i18n')('directDeposit.account.label.bank.name')},
            { title: $filter('i18n')('directDeposit.account.label.routing.num')},
            { title: $filter('i18n')('directDeposit.account.label.account.num')},
            { title: $filter('i18n')('directDeposit.account.label.accountType')},
            { title: $filter('i18n')('directDeposit.account.label.status')}
        ];
        
        // Most Recent Pay
        $scope.mostRecentPayColumns = [
            { tabindex: '0', title: $filter('i18n')('directDeposit.account.label.bank.name')},
            { title: $filter('i18n')('directDeposit.account.label.routing.num')},
            { title: $filter('i18n')('directDeposit.account.label.account.num')},
            { title: $filter('i18n')('directDeposit.account.label.accountType')},
            { title: $filter('i18n')('directDeposit.label.distribution.net.pay')}
        ];
        
        // Proposed Pay
        $scope.proposedPayColumns = [
            { tabindex: '0', title: $filter('i18n')('directDeposit.account.label.bank.name')},
            { title: $filter('i18n')('directDeposit.account.label.routing.num')},
            { title: $filter('i18n')('directDeposit.account.label.account.num')},
            { title: $filter('i18n')('directDeposit.account.label.accountType')},
            { title: $filter('i18n')('directDeposit.account.label.amount')},
            { title: $filter('i18n')('directDeposit.account.label.priority')},
            { title: $filter('i18n')('directDeposit.label.distribution.net.pay')},
            { title: $filter('i18n')('directDeposit.account.label.status')}
        ];

        //display add/edit account pop up
        $scope.showEditAccount = function (typeInd, isAddNew) {
            //if(isAddNew && typeInd === 'AP' && $scope.hasApAccount) return;
            if(isAddNew) {
                // If this is an AP account and an AP account already exists, this functionality is disabled
                if (typeInd === 'AP' && $scope.hasApAccount) {
                    return;
                }

                if (typeInd === 'HR' && $scope.hasMaxPayrollAccounts) {
                    notificationCenterService.displayNotifications('directDeposit.max.payroll.accounts.text', 'error');
                    return;
                }
            }

            // Otherwise, open modal
            var acctList = [];
            
            if(isAddNew && $scope.isEmployee){
                var allocs = $scope.distributions.proposed.allocations;
                
                if(typeInd === 'HR' && $scope.account){
                    acctList[0] = $scope.account;
                }
                else if (typeInd === 'AP' && allocs.length > 0){
                    acctList = allocs;
                }
            }
            
            var modal = $modal.open({
                templateUrl: '../generalSsbApp/ddEditAccount/ddEditAccount.html',
                windowClass: 'edit-account-modal',
                keyboard: true,
                controller: "ddEditAccountController",
                scope: $scope,
                resolve: {
                    editAcctProperties: function(){
                        return { 
                            typeIndicator: typeInd, 
                            creatingNew: !!isAddNew,
                            otherAccounts: acctList
                        };
                    }
                }
            });
        };

        $scope.isDesktop = function () {
            return isDesktop();
        };

        $scope.getNoPayAllocationsNotificationText = function () {
            return ($scope.isDesktop()) ?
                'directDeposit.notification.no.payroll.allocation.click' :
                'directDeposit.notification.no.payroll.allocation.tap';
        };

        $scope.getNoApAllocationsNotificationText = function () {
            return ($scope.isDesktop()) ?
                'directDeposit.notification.no.accounts.payable.allocation.click' :
                'directDeposit.notification.no.accounts.payable.allocation.tap';
        };

        $scope.updateAccounts = function () {
            var allocs = $scope.distributions.proposed.allocations
            if($scope.isEmployee){
                var i;
                for(i = 0; i < allocs.length; i++){
                    updateAccount($scope.distributions.proposed.allocations[i]);
                }
            }
            // AP account will already be updated if it is synced with a Payroll account
            if($scope.hasApAccount && !ddEditAccountService.syncedAccounts){
                updateAccount($scope.account);
            }
        };
        
        var updateAccount = function (acct) {
            if(acct.hrIndicator === 'A'){
                ddEditAccountService.setAmountValues(acct, acct.amountType);
            }

            ddEditAccountService.saveAccount(acct).$promise.then(function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    if(acct.version != response.version &&
                            ddEditAccountService.syncedAccounts === acct.bankAccountNum){
                        notificationCenterService.displayNotifications("Account "+ddEditAccountService.syncedAccounts+" updated automatically", "success");
                    }

                    // Refresh account info
                    acct.version = response.version;

                    // Set form back to initial "at rest" state
                    $scope.authorizedChanges = false;
                }
            });
        };

        $scope.toggleApAccountSelectedForDelete = function () {
            $scope.apAccountSelectedForDelete = !$scope.apAccountSelectedForDelete;
        };

        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.deleteAccount = function () {
            var accounts = [];

            accounts.push($scope.account);

            ddEditAccountService.deleteAccounts(accounts).$promise.then(function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    // Refresh account info
                    $scope.account = null;
                    $scope.cancelNotification();
                    $state.go('directDepositListing', {}, {reload: true, inherit: false, notify: true});
                }
            });
        };

        // Display accounts payable Delete Account confirmation modal
        $scope.confirmAPDelete = function () {
            // If no account is selected for deletion, this functionality is disabled
            if (!$scope.apAccountSelectedForDelete) return;

            var prompts = [
                {
                    label: "Cancel",
                    action: $scope.cancelNotification
                },
                {
                    label: "Delete",
                    action: $scope.deleteAccount
                }
            ];

            notificationCenterService.displayNotifications('directDeposit.confirm.ap.delete.text', 'warning', false, prompts);
        };

        $scope.toggleAuthorizedChanges = function () {
            $scope.authorizedChanges = !$scope.authorizedChanges;
        };

        $scope.setAccountType = function (acctType) {
            $scope.account.accountType = acctType;
        };

        $scope.updateWhetherHasMaxPayrollAccounts = function () {
            if (!$scope.distributions.proposed) {
                $scope.hasMaxPayrollAccounts = false;
            }

            directDepositService.getConfiguration().$promise.then(
                function(response) {
                    var numAllocatons = $scope.distributions.proposed.allocations.length;

                    $scope.hasMaxPayrollAccounts = numAllocatons >= response.MAX_USER_PAYROLL_ALLOCATIONS;
            });
        };


        // INITIALIZE
        // ----------
        this.init();
    }
]);
