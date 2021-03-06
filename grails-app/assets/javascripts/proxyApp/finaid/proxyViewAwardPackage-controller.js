/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
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

        $scope.aidYearFetcher = proxyAppService.getAidYears;
        $scope.onSelect = function () {
            proxyAppService.setAidYear($scope.aidYearHolder.aidYear);
            if(!$scope.aidYearHolder.aidYear.code) {
                $scope.awardPackage = {};
                $scope.showMessageForNoAwardInfo = false;
                $scope.$apply();
            } else {
                getAwardPackage();
            }
        };

         var getAwardPackage = function() {
            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getAwardPackage({aidYear: $scope.aidYearHolder.aidYear.code, id: sessionStorage.getItem("id")}).$promise.then(function (response) {
                    if(response.failure || !response.hasAwardInfo) {
                        $scope.showMessageForNoAwardInfo = true;
                        $scope.awardPackage = {};
                    }
                    else {
                        $scope.awardPackage = response;
                        $scope.showMessageForNoAwardInfo = !response.hasAwardInfo;
                    }
                });
            } else {
                $scope.showMessageForNoAwardInfo = false;
            }
        };

        var init = function() {
            getAwardPackage();
        };

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
                return (statusData[0].length ? statusData[0] + ': ' : '') + $filter('i18n')('proxy.awardPackage.termStatus.unknown');
            }
            else {
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

            return periodAward ? periodAward : {};
        };

        init();
    }
]);
