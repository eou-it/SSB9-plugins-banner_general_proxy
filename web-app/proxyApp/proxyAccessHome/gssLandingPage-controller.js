/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyAppControllers.controller('gssLandingPageController',['$scope', 'proxyAppService',
    function ($scope, proxyAppService) {

        // LOCAL VARIABLES
        // ---------------
        var STUDENT = 0,
            EMPLOYEE = 1,
            AIPADMIN =2,


        // LOCAL FUNCTIONS
        // ---------------
            init = function() {

                //if (CommonContext.guestUser){

                    sessionStorage.setItem('proxyLandingPage', window.parent.location);

                    //disable the menu for proxy
                    $('#menuContainer').removeClass('show').addClass('hide');
                    $('#menu').removeClass('show').addClass('hide');
                    $('#bannerMenu').removeClass('show').addClass('hide');


                    proxyAppService.getStudentListForProxy().$promise.then(function (response) {
                        $scope.students = response.students;
                        $scope.proxyUser = toCamelCase(response.proxyProfile.p_first_name) + " " + toCamelCase(response.proxyProfile.p_last_name);

                        _.each($scope.students, function(student) {

                            $scope.proxyTiles.push(
                                {
                                    title: "I am proxy for: " ,
                                    desc: student.name,
                                    url: $scope.applicationContextRoot +'/ssb/proxy/proxypersonalinformation',
                                    icon: '../images/personal_info.svg',
                                    pages : student.pages,
                                    pidm: student.pidm,
                                    open : false
                                }
                            );

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



               // }

            };

        function toCamelCase(str){
            return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        }



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
