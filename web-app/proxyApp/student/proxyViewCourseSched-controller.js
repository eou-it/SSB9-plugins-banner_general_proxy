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
        $scope.tgtDate = null;

        $scope.goToDate = function(date) {
            if(date) {
                $('#calendar').fullCalendar( 'gotoDate', date);
                $scope.tgtDate = date;
            }
        };

    }
]);
