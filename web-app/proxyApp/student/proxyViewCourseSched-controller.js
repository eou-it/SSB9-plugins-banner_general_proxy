/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewCourseSchedController',['$scope', '$rootScope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.schedule = {};
        $scope.scheduleConflicts = {};
        $scope.unassignedSchedule = {};
        $scope.hasPrevWeek = false;
        $scope.hasNextWeek = false;
        $scope.pidm = $stateParams.pidm;
        $scope.studentName = proxyAppService.getStudentName();

        proxyAppService.getCourseSchedule({pidm: $stateParams.pidm}).$promise.then(function(response) {
            $scope.schedule = response.schedule;
            $scope.scheduleConflicts = response.scheduleConflicts;
            $scope.unassignedSchedule = response.unassignedSchedule;
            $scope.hasPrevWeek = response.hasPrevWeek;
            $scope.hasNextWeek = response.hasNextWeek;
        });

    }
]);
