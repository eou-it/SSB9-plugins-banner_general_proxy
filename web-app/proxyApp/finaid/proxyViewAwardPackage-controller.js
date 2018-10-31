/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardPackage',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.id = $stateParams.id;
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
                proxyAppService.getAwardPackage({aidYear: $scope.aidYearHolder.aidYear.code, id: sessionStorage.getItem("id")}).$promise.then(function (response) {
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
            var textKey = '';
            switch (option) {
                case 1:
                    textKey = 'proxy.awardPackage.nonPell.fullTime';
                    break;
                case 2:
                    textKey = 'proxy.awardPackage.nonPell.threeFourths';
                    break;
                case 3:
                    textKey = 'proxy.awardPackage.nonPell.halfTime';
                    break;
                case 4:
                    textKey = 'proxy.awardPackage.nonPell.lessHalf';
                    break;
            }

            return $filter('i18n')(textKey);
        };

        $scope.now = function() {
            return moment().format('YYYY-MM-DD');
        };

        $scope.getFundList = function() {
            if($scope.awardPackage.periodInfo) {
                return Object.keys($scope.awardPackage.periodInfo.fundTotals);
            }
            else {
                return [];
            }
        };

        init();
    }
]);
