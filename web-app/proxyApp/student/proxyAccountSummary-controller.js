/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAccountSummaryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope,$rootScope, $stateParams, proxyAppService, $filter) {

        $scope.accountSummary = {};
        $scope.studentName = proxyAppService.getStudentName();
        $scope.payvendUrl = null;
        $scope.payvendVendor = null;

        proxyAppService.getConfiguration().$promise.then(function(response) {
            $scope.payvendUrl = response.PAYVEND_URL;
            $scope.payvendVendor = response.PAYVEND_VENDOR;
        });

        proxyAppService.getAccountSummary({pidm: sessionStorage.getItem("pidm")}).$promise.then(function(response) {
            $scope.accountSummary = response;
            _.each(
                _.filter($scope.accountSummary.terms, function(term) {
                    return (term.termCode === 'ARTERM');
                }),
                function(term) {
                    term.termDesc = $filter('i18n')('proxy.acctSummary.label.arterm');
                }
            );
        });

    }
]);
