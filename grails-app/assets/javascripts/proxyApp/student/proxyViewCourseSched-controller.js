/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewCourseSchedController',['$scope', '$rootScope', 'proxyAppService', 'proxyAppDateService',
    function ($scope, $rootScope, proxyAppService, proxyAppDateService) {

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
                var isoDate = moment(proxyAppDateService.stringToDate(date));
                $('#calendar').fullCalendar( 'gotoDate', isoDate);
                $scope.tgtDate = date;
            }
        };

    }
]);
