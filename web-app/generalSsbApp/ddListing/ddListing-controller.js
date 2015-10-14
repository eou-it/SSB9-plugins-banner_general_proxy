/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', '$filter',
    'directDepositListingService', 'directDepositEditAccountService', 'notificationCenterService',
    function ($scope, $modal, $filter, directDepositListingService, directDepositEditAccountService,
              notificationCenterService){

        // LOCAL FUNCTIONS
        // ---------------
        /**
         * Select the one AP account that will be displayed to user, according to business rules.
         */
        this.getApAccountFromResponse = function(response) {
            var account = null;

            if (response.length) {
                // TODO: until defined otherwise, return first account
                account = response[0];
            }

            return account;
        };

        /**
         * Initialize controller
         */
        this.init = function() {
            var self = this;

            directDepositListingService.getDirectDepositListing().$promise.then(
                function (response) {
                    $scope.account = self.getApAccountFromResponse(response);
                    $scope.accountLoaded = true;
                });
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.account = null;
        $scope.accountLoaded = false;
        $scope.panelCollapsed = false;
        $scope.authorizedChanges = false;


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.apListingColumns = [
            { tabindex: '0', title: $filter('i18n')('directDeposit.account.label.bank.name')},
            { title: $filter('i18n')('directDeposit.account.label.routing.num')},
            { title: $filter('i18n')('directDeposit.account.label.account.num')},
            { title: $filter('i18n')('directDeposit.account.label.accountType')},
            { title: $filter('i18n')('directDeposit.account.label.status')}
        ];

        //display add account pop up
        $scope.showAddAccount = function () {
            var mobile = true;
            if(mobile) {
                $modal.open({
                    templateUrl: '../generalSsbApp/ddAddAccount/ddAddAccount.html',
                    keyboard:true,
                    controller: "DdAddAccountController",
                    scope: $scope
                });
            }
            else {
                // enable in-line editing(?) or go to select from existing acct page
            }
        };

        $scope.hasAccount = function () {
            return !!$scope.account;
        };

        $scope.isDesktop = function () {
            return isDesktop();
        };

        $scope.getNoApAllocationsNotificationText = function () {
            return ($scope.isDesktop()) ?
                'directDeposit.notification.no.accounts.payable.allocation.click' :
                'directDeposit.notification.no.accounts.payable.allocation.tap';
        };

        $scope.updateApAccount = function () {
            directDepositEditAccountService.updateAccount($scope.account).$promise.then(function (response) {
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

        // Display Edit Account modal
        $scope.showEditAccountModal = function () {
            $modal.open({
                templateUrl: '../generalSsbApp/ddAddAccount/ddAddAccount.html',
                keyboard:true,
                controller: "DdAddAccountController",
                scope: $scope
            });
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
