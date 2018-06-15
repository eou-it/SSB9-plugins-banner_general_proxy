/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyAppControllers.controller('gssLandingPageController',['$scope', 'proxyAppService', 'piConfigResolve',
    function ($scope, generalSsbService, piConfigResolve) {

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

                    $scope.proxyTiles.push(
                        {
                            title: 'banner.generalssb.landingpage.personal.title',
                            desc: 'banner.generalssb.landingpage.personal.description',
                            url: $scope.applicationContextRoot +'/ssb/proxy/proxypersonalinformation',
                            icon: '../images/personal_info.svg'
                        }
                    );


                    $scope.proxyTiles.push(
                        {
                            title: 'banner.generalssb.landingpage.grades.title',
                            desc: 'banner.generalssb.landingpage.grades.description',
                            url: $scope.applicationContextRoot +'/ssb/proxy/grades',
                            icon: '../images/personal_info.svg'
                        }
                    );

                    $scope.proxyTiles.push(
                        {
                            title: 'banner.generalssb.landingpage.holds.title',
                            desc: 'banner.generalssb.landingpage.holds.description',
                            url: $scope.applicationContextRoot +'/ssb/proxy/holds',
                            icon: '../images/personal_info.svg'
                        }
                    );

                }

            };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.piConfig = {};
        $scope.proxyTiles = [];
        $scope.isSingleTile;
        $scope.firstName = '';
        $scope.bannerId;


        // INITIALIZE
        // ----------
        init();

    }
]);
