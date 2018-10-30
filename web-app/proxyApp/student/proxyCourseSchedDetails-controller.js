/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyCourseSchedDetails',['$scope','$rootScope','$stateParams', 'proxyAppService', '$filter',
    function ($scope, $rootScope, $stateParams, proxyAppService, $filter) {

        $scope.schedule = {};
        $scope.termHolder = {
            term: {}
        };
        if(proxyAppService.getTerm()) {
            $scope.termHolder.term = proxyAppService.getTerm();
        }

        var getDetailSchedule = function() {
            if($scope.termHolder.term.code) {
                proxyAppService.getDetailSchedule({termCode: $scope.termHolder.term.code, id: $scope.id}).$promise.then(function (response) {
                    $scope.schedule = response.rows;
                    $scope.errorMsg = response.errorMsg;

                    $scope.totalCredits = $scope.schedule[0] ? $scope.schedule[0].tot_credits : null;
                    $scope.schedule.forEach(function (course) {
                        course.instructorsText = _.reduce(course.instructors.rows, function (memo, instructorObj, index) {
                            if (index === 0) {
                                return instructorObj.instructor;
                            }
                            else {
                                return memo + ', ' + instructorObj.instructor;
                            }
                        }, '');

                        course.tbl_meetings.forEach(function (meeting) {
                            meeting.schedTypeText = _.reduce(meeting.sched_type, function (memo, sched_type, index) {
                                return memo + ', ' + sched_type;
                            });

                            if(meeting.where.length > 0) {
                                meeting.whereText = _.reduce(meeting.where, function (memo, location, index) {
                                    return memo + ', ' + location;
                                });
                            }
                            else {
                                meeting.whereText = $filter('i18n')('proxy.schedule.Tba');
                            }

                        });
                    });
                });
            }
        },
        init = function() {

            $scope.id = sessionStorage.getItem('id');
            $scope.studentName = proxyAppService.getStudentName();

            $('#term', this.$el).on('change', function (event) {
                proxyAppService.setTerm($scope.termHolder.term);
                if(event.target.value !== 'not/app') { // don't run query on "Not Applicable" selection
                    getDetailSchedule();
                }
                else {
                    $scope.termHolder.term.code = null;
                    $scope.schedule = [];
                    $scope.errorMsg = 'notRegistered';
                    $scope.$apply();
                }
            });

            getDetailSchedule();
        };

        init();
    }
]);
