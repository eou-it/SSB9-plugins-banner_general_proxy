/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', 'directDepositListingService',
    function ($scope, $modal, directDepositListingService){
        $scope.accounts = [];
        $scope.accountsLoaded = false;
        $scope.numAccounts = 0;
        $scope.panelCollapsed = false;

        directDepositListingService.getDirectDepositListing().$promise.then(
            function (response) {
                // Create accounts array, merging account info with its bank name and routing number
                _.each(response, function(accountInfo) {
                    var acct = accountInfo[0];
                    acct.bankName = accountInfo[1].bankName;
                    $scope.accounts.push(acct);
                });

                $scope.numAccounts = $scope.accounts.length;
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


    }
]);
