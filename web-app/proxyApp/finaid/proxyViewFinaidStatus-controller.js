/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewFinaidStatusController',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        var sortFinancialAidStatusLines = function(finaidStatus) {
            var retArr = [];

            // Push values, if they exist, into array in a specific order for consistent display to user.
            if (finaidStatus.hasOwnProperty('unSatReq') && finaidStatus.unSatReq) {
                retArr.push(finaidStatus.unSatReq);
            }

            if (finaidStatus.hasOwnProperty('costOfAttendance') && finaidStatus.costOfAttendance) {
                retArr.push(finaidStatus.costOfAttendance);
            }

            if (finaidStatus.hasOwnProperty('awardPackage') && finaidStatus.awardPackage) {
                retArr.push(finaidStatus.awardPackage);
            }

            if (finaidStatus.hasOwnProperty('accountSummary') && finaidStatus.accountSummary) {
                retArr.push(finaidStatus.accountSummary);
            }

            if (finaidStatus.hasOwnProperty('financialAidHistory') && finaidStatus.financialAidHistory) {
                retArr.push(finaidStatus.financialAidHistory);
            }

            if (finaidStatus.hasOwnProperty('finAidHolds') && finaidStatus.finAidHolds) {
                retArr.push(finaidStatus.finAidHolds);
            }

            return retArr;
        };

        $scope.financialAidStatus = [];
        $scope.aidYearHolder = {
            aidYear: {}
        };
        $scope.aidYears = [];

        $scope.stringifyFinaidStatusMessageFor = function(statusLine) {
            var translatedText = $filter('i18n')('proxy.finaid.status.message.' + statusLine.text, statusLine.textParams);

            return translatedText ? translatedText : statusLine.text;
        };

        $scope.id = $stateParams.id;
        $scope.studentName = proxyAppService.getStudentName();

        $scope.onAidYearSelect = function () {
            proxyAppService.setAidYear($scope.aidYearHolder.aidYear);
            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getFinancialAidStatus({aidYear: $scope.aidYearHolder.aidYear.code, id: $scope.id}).$promise.then(function (response) {
                    $scope.financialAidStatus = sortFinancialAidStatusLines(response);
                });
            }
        };
        var curPage = 0, stopLoading = false;
        $scope.refreshData = function(search, loadingMore) {
            if (!loadingMore) {
                // new search
                $scope.aidYears = [];
                curPage = 0;
                stopLoading = false;
            }

            if(!$scope.isLoading && !stopLoading) {
                if(loadingMore) {
                    // get more results from current search
                    curPage++;
                }

                $scope.isLoading = true;
                proxyAppService.getAidYears({
                    searchString: search ? search : '',
                    offset: curPage,
                    max: 10
                }).$promise.then(function (response) {
                    $scope.aidYears = $scope.aidYears.concat(response);
                    $scope.isLoading = false;
                    if (response.length < 10) {
                        stopLoading = true; // we found everything
                    }
                });
            }
        };

        var init = function() {

            if(proxyAppService.getAidYear()) {
                $scope.aidYearHolder.aidYear = proxyAppService.getAidYear();
            }

            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getFinancialAidStatus({aidYear: $scope.aidYearHolder.aidYear.code, id: sessionStorage.getItem("id")}).$promise.then(function (response) {
                    $scope.financialAidStatus = sortFinancialAidStatusLines(response);
                });
            }
        };

        init();
    }
]);
