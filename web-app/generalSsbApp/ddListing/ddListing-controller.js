/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$state', '$modal', '$filter',
    'ddListingService', 'ddEditAccountService', 'notificationCenterService',
    function ($scope, $state, $modal, $filter, ddListingService, ddEditAccountService,
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

            ddListingService.getApListing().$promise.then(
                function (response) {
                    // By default, set A/P account as currently active account, as it can be edited inline (in desktop
                    // view), while payroll accounts can not be.
                    $scope.account = self.getApAccountFromResponse(response);
                    $scope.hasApAccount = !!$scope.account;
                    $scope.accountLoaded = true;
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
                }
            });

            ddListingService.getUserPayrollAllocationListing().$promise.then( function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    $scope.distributions.proposed = response;
                    $scope.hasPayAccountsProposed = !!response.length
                    $scope.payAccountsProposedLoaded = true;
                }
            });
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
		    { title: $filter('i18n')('directDeposit.label.distribution.net.pay')}
		];

        //display add/edit account pop up
        $scope.showEditAccount = function (typeInd, isAddNew) {
            // If this is an AP account and an AP account already exists, this functionality is disabled
            if(isAddNew && typeInd === 'AP' && $scope.hasApAccount) return;

            // Otherwise, open modal
            var modal = $modal.open({
                templateUrl: '../generalSsbApp/ddEditAccount/ddEditAccount.html',
                windowClass: 'edit-account-modal',
                keyboard: true,
                controller: "ddEditAccountController",
                scope: $scope,
                resolve: {
                    editAcctProperties: function(){
                        return { typeIndicator: typeInd, creatingNew: !!isAddNew };
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

        $scope.updateAccount = function () {
            ddEditAccountService.saveAccount($scope.account).$promise.then(function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                } else {
                    // Refresh account info
                    $scope.account.version = response.version;

                    // Set form back to initial "at rest" state
                    $scope.authorizedChanges = false;

                    // TODO: show confirmation message or something here?
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


        // INITIALIZE
        // ----------
        this.init();

    }
]);
