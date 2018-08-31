/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardPackage',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.pidm = $stateParams.pidm;
        $scope.aidYear = '';
        $scope.awardPackage = {test: 'bonkers'};
        $scope.studentName = proxyAppService.getStudentName();

        $scope.submit = function() {
            proxyAppService.getAwardPackage({aidYear: $scope.aidYear, pidm: $scope.pidm}).$promise.then(function (response) {
                $scope.awardPackage = response;
            });
        };

        $scope.getStatusTextNonPell = function(option) {
            var text = '';
            switch (option) {
                case 1:
                    text = 'Full-Time';
                    break;
                case 2:
                    text = '3/4 Time';
                    break;
                case 3:
                    text = '1/2 Time';
                    break;
                case 4:
                    text = 'Less than 1/2 Time';
                    break;
            }

            return text;
        };

        $scope.now = function() {
            return moment().format('YYYY-MM-DD');
        };
    }
]);
