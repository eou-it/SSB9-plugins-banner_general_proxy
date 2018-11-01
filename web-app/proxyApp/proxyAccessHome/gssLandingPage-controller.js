/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyAppControllers.controller('gssLandingPageController',['$scope', '$rootScope', '$location', '$stateParams', '$timeout', 'proxyAppService', 'notificationCenterService',
    function ($scope, $rootScope, $location, $stateParams, $timeout, proxyAppService, notificationCenterService) {

        // LOCAL FUNCTIONS
        // ---------------
        var toCamelCase = function(str) {
            return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
        },

        /**
         * Show any notifications slated to be shown on state load.
         * (The timeout is needed in cases where the common platform control bar needs time to load. It
         * may be that it's not a typical concern -- would only affect showing notifications on initial
         * page load -- but it's barely noticeable so doesn't hurt to leave it.)
         */
        displayNotificationsOnStateLoad = function() {
            $timeout(function() {
                _.each($stateParams.onLoadNotifications, function(notification) {
                    notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                });
            }, 200);
        },

        init = function() {

            if ($rootScope.profileRequired){

                if ($('#breadcrumb-panel').is(":visible")) {
                    $("#breadcrumb-panel").hide();
                }

                $location.path("/proxypersonalinformation");

                return;
            }

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
                            id: student.id,
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

            displayNotificationsOnStateLoad();
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


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.initLandingPage = function(profileReq)
        {
            //This function is sort of private constructor for controller
            $rootScope.profileRequired = profileReq;

        };



        // INITIALIZE
        // ----------
        init();

    }
]);
