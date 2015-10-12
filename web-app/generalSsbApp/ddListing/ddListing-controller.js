/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', '$filter',
    'directDepositListingService', 'directDepositEditAccountService',
    function ($scope, $modal, $filter, directDepositListingService, directDepositEditAccountService){

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
        $scope.editAccountService = directDepositEditAccountService;

        $scope.account = null;
        $scope.accountLoaded = false;
        $scope.panelCollapsed = false;


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


        // INITIALIZE
        // ----------
        this.init();

    }
]);
