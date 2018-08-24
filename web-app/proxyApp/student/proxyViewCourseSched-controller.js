/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewCourseSchedController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.schedule = {};
        $scope.unassignedSchedule = {};
        $scope.hasPrevWeek = false;
        $scope.hasNextWeek = false;
        $scope.pidm = $stateParams.pidm;

        $("[class*='breadcrumbButton']").append(" " + $filter('i18n')('proxy.schedule.label') + " " + $rootScope.studentName);

        proxyAppService.getCourseSchedule({pidm: $stateParams.pidm}).$promise.then(function(response) {
            $scope.schedule = response.schedule;
            $scope.unassignedSchedule = response.unassignedSchedule;
            $scope.hasPrevWeek = response.hasPrevWeek;
            $scope.hasNextWeek = response.hasNextWeek;
        });

    }
]);
