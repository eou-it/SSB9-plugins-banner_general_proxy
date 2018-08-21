/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewCourseSchedController',['$scope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $stateParams, proxyAppService, $filter) {

        $scope.schedule = {};
        $scope.unassignedSchedule = {};
        $scope.hasPrevWeek = false;
        $scope.hasNextWeek = false;
        $scope.pidm = $stateParams.pidm;

        proxyAppService.getCourseSchedule({pidm: $stateParams.pidm}).$promise.then(function(response) {
            $scope.schedule = response.schedule;
            $scope.unassignedSchedule = response.unassignedSchedule;
            $scope.hasPrevWeek = response.hasPrevWeek;
            $scope.hasNextWeek = response.hasNextWeek;
        });

    }
]);
