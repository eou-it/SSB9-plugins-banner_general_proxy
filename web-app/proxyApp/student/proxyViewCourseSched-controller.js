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

        $scope.goToDate = function() {
            if($scope.tgtDate) {
                $('#calendar').fullCalendar( 'gotoDate', $scope.tgtDate);
                $scope.tgtDate = undefined;
            }
        };

    }
]);
