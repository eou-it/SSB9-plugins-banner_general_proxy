/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', '$filter', 'directDepositListingService',
    function ($scope, $modal, $filter, directDepositListingService){
        $scope.accounts = [];
        $scope.accountsLoaded = false;
        $scope.panelCollapsed = false;

        $scope.apListingColumns = [
            { tabindex: '0', title: $filter('i18n')('directDeposit.account.label.bank.name')},
            { title: $filter('i18n')('directDeposit.account.label.routing.num')},
            { title: $filter('i18n')('directDeposit.account.label.account.num')},
            { title: $filter('i18n')('directDeposit.account.label.accountType')},
            { title: $filter('i18n')('directDeposit.account.label.status')}
        ];

        directDepositListingService.getDirectDepositListing().$promise.then(
            function (response) {
                // Create accounts array, merging account info with its bank name and routing number
                _.each(response, function(accountInfo) {
                    var acct = accountInfo[0];
                    acct.bankName = accountInfo[1].bankName;
                    $scope.accounts.push(acct);
                });

                $scope.accountsLoaded = true;
            });

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

        $scope.hasAccounts = function () {
            var accounts = $scope.accounts;
            return (accounts && accounts.length > 0);
        };

        $scope.isDesktop = function () {
            return isDesktop();
        };

        $scope.isNotDesktopAndHasAccounts = function () {
            return !$scope.isDesktop() && $scope.hasAccounts();
        };

        $scope.isDesktopWithAccounts = function () {
            return $scope.isDesktop() && $scope.hasAccounts();
        };

        $scope.getNoApAllocationsNotificationText = function () {
            return ($scope.isDesktop()) ?
                'directDeposit.notification.no.accounts.payable.allocation.click' :
                'directDeposit.notification.no.accounts.payable.allocation.tap';
        }

    }
]);
