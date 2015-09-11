/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', '$modal', 'directDepositListingService',
    function ($scope, $modal, directDepositListingService){
        $scope.panelCollapsed = false;

        directDepositListingService.getDirectDepositListing().$promise.then(
            function (response) {
                $scope.accounts = response
            })

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
