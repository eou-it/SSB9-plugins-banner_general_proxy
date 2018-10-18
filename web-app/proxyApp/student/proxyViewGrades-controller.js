/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewGradesController',['$scope', 'proxyAppService',
    function ($scope, proxyAppService) {

        $scope.student = {
            name: proxyAppService.getStudentName(),
            grades: []
        };

        $scope.term = {
            code: {}
        };


        if(proxyAppService.getTerm()) {
            $scope.term.code = proxyAppService.getTerm();

            proxyAppService.getGrades({termCode: $scope.term.code.code}).$promise.then(function(response) {

                $scope.student.grades = response.data;
                
            });
        }
    }
]);
