/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewFinaidStatusController',['$scope', '$state','$rootScope','$stateParams', 'proxyAppService', '$filter', 'notificationCenterService',
    function ($scope, $state, $rootScope, $stateParams, proxyAppService, $filter, notificationCenterService) {

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

        $scope.aidYearFetcher = proxyAppService.getAidYears;
        $scope.onAidYearSelect = function () {
            proxyAppService.setAidYear($scope.aidYearHolder.aidYear);
            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getFinancialAidStatus({aidYear: $scope.aidYearHolder.aidYear.code, id: $scope.id}).$promise.then(function (response) {
                    handleResponse(response);
                });
            }
        };

        $scope.goTo = function(url) {
            $state.go(url,{id: $stateParams.id ? $stateParams.id : sessionStorage.getItem("id")});
        };

        
        var init = function() {

            if(proxyAppService.getAidYear()) {
                $scope.aidYearHolder.aidYear = proxyAppService.getAidYear();
            }

            if($scope.aidYearHolder.aidYear.code) {
                proxyAppService.getFinancialAidStatus({aidYear: $scope.aidYearHolder.aidYear.code, id: sessionStorage.getItem("id")}).$promise.then(function (response) {
                    handleResponse(response);
                });
            }
        };


        var handleResponse = function(response){
            if(response.failure) {
                $scope.financialAidStatus=[];
                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(response.message, "error", true);
            }else {
                $scope.financialAidStatus = sortFinancialAidStatusLines(response);
            }
        }

        init();

    }
]);
