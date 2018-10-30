/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewHoldsController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope,$rootScope, $stateParams, proxyAppService, $filter) {

        $scope.holds = {};
        $scope.studentName = proxyAppService.getStudentName();

        proxyAppService.getHolds({pidm: sessionStorage.getItem("pidm")}).$promise.then(function(response) {
            $scope.holds = response;
            $scope.holds.message = 'noHoldsExist';
        });

    }
]);
