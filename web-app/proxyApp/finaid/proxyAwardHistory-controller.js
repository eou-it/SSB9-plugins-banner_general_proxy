/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardHistoryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter','$sce', '$state','notificationCenterService',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter, $sce, $state, notificationCenterService) {

        $scope.awards = {};
        $scope.messages = [];
        $scope.aidYear = "";
        $scope.studentName = proxyAppService.getStudentName();


        var load = function() {
            proxyAppService.getAwardHistory({id: $stateParams.id ? $stateParams.id : sessionStorage.getItem("id")}).$promise.then(function (response) {

                if (typeof response.awards !== 'undefined' && response.awards.length > 0) {

                    $scope.awards = response.awards;

                }

                $scope.messages = response.messages;

            });
        };


        $scope.stringifyAwardMesageFor = function(message) {

            return ($filter('i18n')('proxy.awardHistory.message.'+ message) ? $sce.trustAsHtml("<b>" + $filter('i18n')('proxy.awardHistory.message.'+ message) + "</b>") : message);

        };

        load();
    }
]);
