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
    }
]);
