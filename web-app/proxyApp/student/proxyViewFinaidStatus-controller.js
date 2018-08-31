/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewFinaidStatusController',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.financialAidStatus = {};
        $scope.aidYearHolder = {
            aidYear: {}
        };

        init = function() {

            $("[class*='breadcrumbButton']").append(" " + $filter('i18n')('proxy.finaid.status.label') + " " + $rootScope.studentName);

            $scope.pidm = $stateParams.pidm;

            $('#aidyear', this.$el).on('change', function (event) {
                if(event.target.value != 'not/app') { // don't run query on "Not Applicable" selection
                    proxyAppService.getFinancialAidStatus({aidYear: event.target.value, pidm: $scope.pidm}).$promise.then(function (response) {
                        $scope.financialAidStatus = response;
                    });
                }
            });
        };

        init();
    }
]);
