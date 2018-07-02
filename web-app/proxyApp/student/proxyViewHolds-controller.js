/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewHoldsController',['$scope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $stateParams, proxyAppService, $filter) {

        $scope.holds = {};

        proxyAppService.getHolds({pidm: $stateParams.pidm}).$promise.then(function(response) {
            $scope.holds = response;
        });

        $scope.stringifyHoldsFor = function(hold) {
            var text = '';
            _.each(hold.hold_for, function(val) {
                    text = text + $filter('i18n')('proxy.holds.type.'+ val) + ' ';
                }
            );

            return text.trim();
        };

    }
]);
