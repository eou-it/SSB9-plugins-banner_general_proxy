/********************************************************************************
 Copyright 2018-2021 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardHistoryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter','$sce',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter, $sce) {

        $scope.awards = {};
        $scope.messages = [];
        $scope.aidYear = "";
        $scope.studentName = proxyAppService.getStudentName();


        var init = function() {
            proxyAppService.getAwardHistory({id: $stateParams.id ? $stateParams.id : sessionStorage.getItem("id")}).$promise.then(function (response) {

                if (typeof response.awards !== 'undefined' && response.awards.length > 0) {

                    $scope.awards = response.awards;

                }

                $scope.messages = response.messages;

            });
        };


        $scope.stringifyAwardMessageFor = function(message) {

            return ($filter('i18n')('proxy.awardHistory.message.'+ message) ? $sce.trustAsHtml("<b>" + $filter('i18n')('proxy.awardHistory.message.'+ message) + "</b>") : message);

        };

        init();
    }
]);
