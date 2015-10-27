/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', '$filter',
    'ddListingService', 'ddEditAccountService', 'notificationCenterService',
    function ($scope, $modal, $filter, ddListingService, ddEditAccountService,
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

            ddListingService.getDirectDepositListing().$promise.then(
                function (response) {
                    $scope.account = self.getApAccountFromResponse(response);
                    $scope.hasAccount = !!$scope.account;
                    $scope.accountLoaded = true;
                });

            // TODO: Everything in this function below this comment is a stub. Do payroll below like AP just above
            $scope.payAccountsProposedLoaded = true;

            // have no allocations yet
            //$scope.distributions = {
            //    mostRecent: {
            //        date: null,
            //        allocations: []
            //    },
            //    proposed: {
            //        allocations: []
            //    }
            //};

            // have allocations
            $scope.distributions = {
                mostRecent: null,
                //mostRecent: {
                //    date: '04/30/2014',
                //    allocations: [
                //        {
                //            bankRoutingInfo: {
                //                bankName: 'First State Bank',
                //                bankRoutingNum: '123456789'
                //            },
                //            bankAccountNum: '555666777',
                //            accountType: 'C',
                //            accountStatus: 'A',
                //            priority: 1,
                //            amount: 500.00
                //        },
                //        {
                //            bankRoutingInfo: {
                //                bankName: 'Second State Bank',
                //                bankRoutingNum: '444455555'
                //            },
                //            bankAccountNum: '111222333',
                //            accountType: 'S',
                //            accountStatus: 'A',
                //            priority: 1,
                //            amount: 700.00
                //        }
                //    ]
                //},
                proposed: {
                    allocations: [
                        {
                            bankRoutingInfo: {
                                bankName: 'Third State Bank',
                                bankRoutingNum: '987654321'
                            },
                            bankAccountNum: '888999000',
                            accountType: 'S',
                            accountStatus: 'A',
                            priority: 1,
                            amount: 500.00,
                            percent: null
                        }
                    ]
               }
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

            $scope.hasPayAccountsProposed = !!$scope.distributions.proposed.allocations;
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
        $scope.account = null;
        $scope.accountLoaded = false;
        $scope.hasAccount = false;
        $scope.panelCollapsed = false;
        $scope.authorizedChanges = false;


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

        //display add account pop up
        $scope.showAddAccount = function () {
            var modal = $modal.open({
                templateUrl: '../generalSsbApp/ddEditAccount/ddEditAccount.html',
                keyboard:true,
                controller: "ddEditAccountController",
                scope: $scope
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

        $scope.updateApAccount = function () {
            ddEditAccountService.saveApAccount($scope.account).$promise.then(function (response) {
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
                templateUrl: '../generalSsbApp/ddEditAccount/ddEditAccount.html',
                keyboard:true,
                controller: "ddEditAccountController",
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
