/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppControllers.controller('ddListingController',['$scope', 'directDepositListingService',
    function ($scope, directDepositListingService){
        $scope.panelCollapsed = false;

        directDepositListingService.getDirectDepositListing().$promise.then(
            function (response) {
                $scope.accounts = response
            })

    }
]);