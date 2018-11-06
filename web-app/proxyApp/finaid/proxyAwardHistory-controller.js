/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardHistoryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter','$sce', '$state','notificationCenterService',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter, $sce, $state, notificationCenterService) {

        $scope.awards = {};
        $scope.messages = {};
        $scope.aidYear = "";
        $scope.studentName = proxyAppService.getStudentName();


        $scope.checkStudentPageForAccess = function() {
            proxyAppService.checkStudentPageForAccess({id: sessionStorage.getItem("id"), name: $state.current.name}).$promise.then(function(response) {

                if(response.failure && !response.authorized) {
                    $scope.flashMessage = response.message;

                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification(response.message, "error", true);

                    if (!response.authorized) {
                        $state.go('home',
                            {reload: true, inherit: false, notify: true}
                        );
                        return;
                    }
                }else{
                    load();
                }
            });

        };


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


    }
]);
