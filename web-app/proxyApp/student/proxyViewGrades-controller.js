/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewGradesController',['$scope', 'proxyAppService', '$stateParams', '$filter',
    function ($scope, proxyAppService , stateParams, filter) {

        $scope.student = {
            name: proxyAppService.getStudentName(),
            grades: []
        };

        $scope.term = {
            code: {}
        };

        $scope.registered = false;


        proxyAppService.getTermsForRegistration().$promise.then(function(response) {
            $scope.registered = response.terms.length > 0;

        });


        if(proxyAppService.getTerm()) {
            $scope.term.code = proxyAppService.getTerm();

            proxyAppService.getGrades({termCode: $scope.term.code.code}).$promise.then(function(response) {

                $scope.student.grades = response.data;
                
            });
        }
    }
]);
