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
        $scope.showMessageForNoAwardInfo = false;

         var getAwardPackage = function() {
            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getAwardPackage({aidYear: $scope.aidYearHolder.aidYear.code, id: sessionStorage.getItem("id")}).$promise.then(function (response) {
                    $scope.awardPackage = response;
                    $scope.showMessageForNoAwardInfo = !$scope.awardPackage.awardInfo;
                });
            } else {
                $scope.showMessageForNoAwardInfo = false;
            }
        };

        var init = function() {
            getAwardPackage();
        };

        $('#aidyear', this.$el).on('change', function (event) {
            proxyAppService.setAidYear($scope.aidYearHolder.aidYear);

            if(event.target.value === 'not/app') {
                // don't run query on "Not Applicable" selection and reset to empty award package
                $scope.awardPackage = {};
                $scope.showMessageForNoAwardInfo = false;
                $scope.$apply();
            } else {
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

        $scope.getStatusTextTerm = function(status) {
            var textKey = '',
                statusData = status.split(':');

            switch (statusData[0]) {
                case 'summer':
                    textKey = 'proxy.awardPackage.termStatus.summer';
                    break;
                case 'fall':
                    textKey = 'proxy.awardPackage.termStatus.fall';
                    break;
                case 'winter':
                    textKey = 'proxy.awardPackage.termStatus.winter';
                    break;
                case 'spring':
                    textKey = 'proxy.awardPackage.termStatus.spring';
                    break;
                case 'nSummer':
                    textKey = 'proxy.awardPackage.termStatus.summer';
                    break;
                default:
                    textKey = 'proxy.awardPackage.termStatus.unknown';
                    statusData[1] = null;
                    break;
            }

            return $filter('i18n')(textKey, [statusData[1]]);
        };

        $scope.getStatusTextTermNew = function(status) {
            var statusData = status.split(':');

            if(statusData[1].trim() === '_unknown_') {
                return statusData[0] + ': ' + $filter('i18n')('proxy.awardPackage.termStatus.unknown');
            }
            else{
                return status;
            }
        };

        $scope.getFundList = function() {
            if($scope.awardPackage.periodInfo) {
                return Object.keys($scope.awardPackage.periodInfo.fundTotals);
            }
            else {
                return [];
            }
        };

        $scope.getFund = function(period, fund) {
            var periodAward = _.find(period.periodAwards, function(award){
                return award.fundTitle === fund;
            });

            if(periodAward) {
                return periodAward;
            }
            else {
                return {};
            }
        };

        init();
    }
]);
