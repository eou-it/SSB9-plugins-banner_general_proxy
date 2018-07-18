/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewGradesController',['$scope', '$stateParams', 'proxyAppService', '$filter',
    function ($scope, $stateParams, proxyAppService, $filter) {

        $scope.grades = {};

        /*
        proxyAppService.getGrades({pidm: $stateParams.pidm}).$promise.then(function(response) {
            $scope.grades = response.data;

            console.log($scope.grades);
        });
        */


        init = function() {

            $('#term', this.$el).on('change', function (event) {
                console.log("Term: " + event.target.value);
                //studentGrades.selectedValueModel.set({
                //   termCode: event.target.value

                proxyAppService.getGrades({termCode: event.target.value}).$promise.then(function(response) {
                    $scope.grades = response.data;

                    console.log($scope.grades);
                });
            });

        }



        $scope.grades = {};

        /*
        $scope.address = {
            county: {},
            state: {},
            nation: {},
            addressType:{},
            city: null,
            fromDate: null,
            toDate: null,
            houseNumber: null,
            streetLine1: null,
            streetLine2: null,
            streetLine3: null,
            streetLine4: null,
            zip: null
        };
        */


        $scope.term = {
            code: {}

        };


       init();


    }
]);
