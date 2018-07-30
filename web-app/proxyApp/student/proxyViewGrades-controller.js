/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewGradesController',['$scope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $stateParams, proxyAppService, $filter) {

        $scope.grades = {};

        init = function() {

            $scope.pidm = $stateParams.pidm

            $('#term', this.$el).on('change', function (event) {
                $scope.pidm = $stateParams.pidm

                proxyAppService.getGrades({termCode: event.target.value, pidm: $scope.pidm}).$promise.then(function(response) {
                    $scope.grades = response.data;

                    //console.log($scope.grades);

                    if ($scope.grades.length > 0){
                        _show();
                }else {
                        _hide();
                    }
                });
            });

        }


        $scope.grades = {};


        $scope.term = {
            code: {}

        };


       init();


        var _hide = function() {

            $('#grades').addClass('ng-hide');

        };

        var _show = function() {

            $( '#grades' ).removeClass('ng-hide');

        };


        $scope.pidm = "";
    }
]);
