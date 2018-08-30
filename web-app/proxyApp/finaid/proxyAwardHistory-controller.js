/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyAwardHistoryController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter','$sce',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter, $sce) {

        $scope.awards = {};
        $scope.messages = {};
        $scope.aidYear = "";


        $("[class*='breadcrumbButton']").append(" " + $filter('i18n')('proxy.awardHsitory.label') + " " + ((typeof $rootScope.studentName != "undefined") ? $rootScope.studentName :sessionStorage.getItem("name"))) ;


        proxyAppService.getAwardHistory({pidm: $stateParams.pidm ? $stateParams.pidm : sessionStorage.getItem("pidm")}).$promise.then(function(response) {

            if (typeof response.awards !== 'undefined' && response.awards.length > 0) {

                $scope.awards = response.awards;

            }

               $scope.messages = response.messages;

        });

        $scope.stringifyAwardMesageFor = function(message) {

            return ($filter('i18n')('proxy.awardHistory.message.'+ message) ? $sce.trustAsHtml("<b>" + $filter('i18n')('proxy.awardHistory.message.'+ message) + "</b>") : message);

        };


    }
]);
