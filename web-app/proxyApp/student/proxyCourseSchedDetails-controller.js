/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyCourseSchedDetails',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.schedule = {};
        $scope.termHolder = {
            term: {}
        };

        init = function() {

            // $("[class*='breadcrumbButton']").append(" " + $filter('i18n')('proxy.scheduleDetails.label') + " " + $rootScope.studentName);

            $scope.pidm = $stateParams.pidm;

            $('#term', this.$el).on('change', function (event) {
                if(event.target.value != 'not/app') { // don't run query on "Not Applicable" selection
                    proxyAppService.getDetailSchedule({termCode: event.target.value, pidm: $scope.pidm}).$promise.then(function (response) {
                        $scope.schedule = response.rows;
                        $scope.errorMsg = response.errorMsg;

                        $scope.totalCredits = $scope.schedule[0] ? $scope.schedule[0].tot_credits : null;
                        $scope.schedule.forEach(function (course) {
                            course.instructorsText = _.reduce(course.instructors.rows, function (memo, instructorObj, index) {
                                if (index === 0) {
                                    return instructorObj.instructor
                                }
                                else {
                                    return memo + ',' + instructorObj.instructor;
                                }
                            }, '');

                            course.tbl_meetings.forEach(function (meeting) {
                                meeting.schedTypeText = _.reduce(meeting.sched_type, function (memo, sched_type, index) {
                                    return memo + ', ' + sched_type
                                });

                                if(meeting.where.length > 0) {
                                    meeting.whereText = _.reduce(meeting.where, function (memo, location, index) {
                                        return memo + ', ' + location
                                    });
                                }
                                else {
                                    meeting.whereText = 'TBA';
                                }

                            });
                        });
                    });
                }
                else {
                    $scope.termHolder.term.code = 'dummy';
                    $scope.schedule = [];
                    $scope.errorMsg = 'notRegistered';
                    $scope.$apply();
                }
            });
        };

        init();
    }
]);
