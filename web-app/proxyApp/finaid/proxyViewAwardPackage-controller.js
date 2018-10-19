/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardPackage',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.pidm = $stateParams.pidm;
        $scope.aidYearHolder = {
            aidYear: {}
        };
        if(proxyAppService.getAidYear()) {
            $scope.aidYearHolder.aidYear = proxyAppService.getAidYear();
        }
        $scope.awardPackage = {};
        $scope.studentName = proxyAppService.getStudentName();

         var getAwardPackage = function() {
            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getAwardPackage({aidYear: $scope.aidYearHolder.aidYear.code, pidm: sessionStorage.getItem("pidm")}).$promise.then(function (response) {
                    $scope.awardPackage = response;
                });
            }
        };

        var init = function() {
            getAwardPackage();
        };

        $('#aidyear', this.$el).on('change', function (event) {
            proxyAppService.setAidYear($scope.aidYearHolder.aidYear);
            if(event.target.value != 'not/app') { // don't run query on "Not Applicable" selection
                getAwardPackage();
            }
        });

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

        init();
    }
]);
