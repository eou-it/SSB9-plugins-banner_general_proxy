/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAccountSummaryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope,$rootScope, $stateParams, proxyAppService, $filter) {

        $scope.accountSummary = {};
        $scope.studentName = proxyAppService.getStudentName();
        $scope.payvendUrl = null;
        $scope.payvendVendor = null;

        function submit_post_via_hidden_form(url, params) {
            var f = $("<form target='TOUCHNET' method='POST' style='display:none;'></form>").attr({
                action: url
            }).appendTo(document.body);

            for (var i in params) {
                if (params.hasOwnProperty(i)) {
                    $('<input type="hidden" />').attr({
                        name: i,
                        value: params[i]
                    }).appendTo(f);
                }
            }

            f.submit();

            f.remove();
        }


        $scope.getAccountBalColor = function() {
            var bal = $scope.accountSummary.accountBal;
            return (bal > 0) ? 'positive-balance' : (bal < 0) ? 'negative-balance' : '';
        };

        $scope.openVendorUrl = function() {

            if ($scope.payvendProcessCenterEnabled){

                submit_post_via_hidden_form(
                    $scope.payvendUrl,
                    {
                        token: $scope.authToken
                    }
                );

            }else {
                window.open($scope.payvendUrl, '_blank');
            }
        };

        proxyAppService.getConfiguration().$promise.then(function(response) {
            $scope.payvendUrl = response.PAYVEND_URL;
            $scope.payvendVendor = response.PAYVEND_VENDOR;
            $scope.payvendProcessCenterEnabled = response.PAYVEND_PROCESS_CENTER_ENABLED
            $scope.authToken = response.autToken;
        });

        proxyAppService.getAccountSummary({id: sessionStorage.getItem("id")}).$promise.then(function(response) {
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
