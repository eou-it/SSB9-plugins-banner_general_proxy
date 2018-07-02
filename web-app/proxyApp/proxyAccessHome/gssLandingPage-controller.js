/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyAppControllers.controller('gssLandingPageController',['$scope', 'proxyAppService', 'piConfigResolve',
    function ($scope, proxyAppService, piConfigResolve) {

        // LOCAL VARIABLES
        // ---------------
        var STUDENT = 0,
            EMPLOYEE = 1,
            AIPADMIN =2,


        // LOCAL FUNCTIONS
        // ---------------
            init = function() {
                $scope.piConfig = piConfigResolve;

                if (CommonContext.guestUser){

                    $scope.guestUser = true;
                    $scope.guestUserName = CommonContext.user;


                    proxyAppService.getStudentListForProxy().$promise.then(function (response) {
                        $scope.students = response.students;

                        _.each($scope.students, function(student) {
                            //console.log("Name: " + student.name)
                            //console.log("PIDM: " + student.pidm)

                            proxyAppService.getPages({pidm: student.pidm}).$promise.then(function (response) {
                                $scope.pages = response.pages;

                            $scope.proxyTiles.push(
                                {
                                    title: "I am proxy for: " ,
                                    desc: student.name,
                                    url: $scope.applicationContextRoot +'/ssb/proxy/proxypersonalinformation',
                                    icon: '../images/personal_info.svg',
                                    pages : $scope.pages,
                                    pidm: student.pidm
                                }
                                  );
                            });
                        });
                    });



                    $scope.appTiles.push(
                        {
                            title: 'banner.generalssb.landingpage.personal.title',
                            desc: 'banner.generalssb.landingpage.personal.description',
                            url: 'proxyPersonalInfo',//$scope.applicationContextRoot +'/ssb/proxy/proxypersonalinformation',
                            icon: '../images/personal_info.svg'
                        }
                    );



                }

            };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.piConfig = {};
        $scope.proxyTiles = [];
        $scope.appTiles = [];
        $scope.isSingleTile;
        $scope.firstName = '';
        $scope.bannerId;
        $scope.pages =[];


        // INITIALIZE
        // ----------
        init();

    }
]);
