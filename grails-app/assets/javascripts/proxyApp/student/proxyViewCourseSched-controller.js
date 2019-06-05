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
        $scope.hasDetailAccess = false;
        $scope.id = sessionStorage.getItem("id");
        $scope.studentName = proxyAppService.getStudentName();
        $scope.tgtDate = null;

        $scope.goToDate = function(date) {
            if(date) {
                var isoDate = moment(proxyAppService.stringToDate(date));
                $('#calendar').fullCalendar( 'gotoDate', isoDate);
                $scope.tgtDate = date;
            }
        };

    }
]);
