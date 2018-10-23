/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewCourseSchedController',['$scope', '$rootScope', 'proxyAppService',
    function ($scope, $rootScope, proxyAppService) {

        $scope.schedule = {};
        $scope.scheduleConflicts = {};
        $scope.unassignedSchedule = {};
        $scope.hasPrevWeek = false;
        $scope.hasNextWeek = false;
        $scope.pidm = sessionStorage.getItem("pidm");
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
