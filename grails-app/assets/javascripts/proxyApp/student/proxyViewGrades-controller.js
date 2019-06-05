/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyViewGradesController',['$scope', 'proxyAppService', '$stateParams', '$filter',
    function ($scope, proxyAppService , stateParams, filter) {

        $scope.student = {
            name: proxyAppService.getStudentName(),
            grades: []
        };

        $scope.termHolder = {
            term: {}
        };

        $scope.registered = false;
        $scope.holds = true;
        $scope.onTermSelect = function () {
            proxyAppService.getGrades({termCode: $scope.termHolder.term.code}).$promise.then(function(response) {
                $scope.student.grades = response.data;

                proxyAppService.setTerm($scope.termHolder.term);
            });
        };
        $scope.termsFetcher = proxyAppService.getTerms;


        proxyAppService.getViewGradesHolds({id: sessionStorage.getItem("id")}).$promise.then(function(response) {
            $scope.holds = response.viewGradesHolds;
        });


        proxyAppService.getTermsForRegistration().$promise.then(function(response) {
            $scope.registered = response.terms.length > 0;
            $scope.terms = response.terms;
        });


        if(proxyAppService.getTerm()) {
            $scope.termHolder.term = proxyAppService.getTerm();

            proxyAppService.getGrades({termCode: $scope.termHolder.term.code}).$promise.then(function(response) {

                $scope.student.grades = response.data;
                
            });
        }
    }
]);
