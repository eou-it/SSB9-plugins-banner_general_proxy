/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope', 'proxyAppService',
    function ($scope, proxyAppService) {

        $scope.proxyProfile = {};
        $scope.proxyUiRules = {};
        $scope.profileElements = {};

        proxyAppService.getProxyPersonalInfo().$promise.then(function(response) {
            $scope.proxyProfile = response.proxyProfile;
            $scope.proxyUiRules = response.proxyUiRules;

            _.each(Object.keys($scope.proxyProfile), function(it){
                var required = $scope.proxyUiRules[it] ? $scope.proxyUiRules[it].required : false;

                $scope.profileElements[it] = {
                    label: required ? ('label.'+it+'*') : ('label.'+it),
                    model: $scope.proxyProfile[it],
                    fieldLength: $scope.proxyUiRules[it].fieldLength,
                    elemId: it,
                    visible: $scope.proxyUiRules[it].visible === undefined ? true : $scope.proxyUiRules[it].visible
                };
            });

            $scope.save = function() {
                console.log($scope.profileElements);
            }
        });
    }
]);
