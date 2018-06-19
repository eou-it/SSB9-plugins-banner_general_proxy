/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope', 'proxyAppService',
    function ($scope, proxyAppService) {

        $scope.proxyProfile = {};

        proxyAppService.getProxyPersonalInfo().$promise.then(function(response) {
            $scope.proxyProfile = response;
        });
    }
]);
