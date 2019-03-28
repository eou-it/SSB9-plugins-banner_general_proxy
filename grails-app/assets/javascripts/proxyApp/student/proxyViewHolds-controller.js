/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewHoldsController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope,$rootScope, $stateParams, proxyAppService, $filter) {

        $scope.holds = {};
        $scope.studentName = proxyAppService.getStudentName();

        proxyAppService.getHolds({id: sessionStorage.getItem("id")}).$promise.then(function(response) {
            $scope.holds = response;
        });

        $scope.stringifyHoldsFor = function(hold) {
            var translatedHolds = [];
            _.each(hold.hold_for, function(val) {
                translatedHolds.push($filter('i18n')('proxy.holds.type.'+ val));
                }
            );

            return translatedHolds.join(" | ");
        };

        $scope.translateHoldProcess = function(hold) {
            return $filter('i18n')('proxy.holds.type.' + hold);

        };
    }
]);
