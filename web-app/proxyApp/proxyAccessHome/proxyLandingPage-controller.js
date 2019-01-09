/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyAppControllers.controller('proxyLandingPageController',['$scope', '$rootScope', '$location', '$stateParams', '$timeout', '$filter', 'proxyAppService', 'notificationCenterService',
    function ($scope, $rootScope, $location, $stateParams, $timeout, $filter, proxyAppService, notificationCenterService) {

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
            //disable tools button
            $('#Preference').removeClass('show').addClass('hide');
            $('#branding').removeAttr('href');

            proxyAppService.getStudentListForProxy().$promise.then(function (response) {
                var addStudentProxyTile = function(student, isActive) {
                    $scope.proxyTiles.push(
                        {
                            desc: student.name,
                            pages : student.pages,
                            selectedPage: {code: null, description: null},
                            id: student.id,
                            active: isActive
                        }
                    );
                };

                $scope.students = response.students;
                $scope.proxyUser = toCamelCase(response.proxyProfile.p_first_name) + " " + toCamelCase(response.proxyProfile.p_last_name);

                _.each($scope.students.active, function(student) {
                    addStudentProxyTile(student, true);
                });

                _.each($scope.students.inactive, function(student) {
                    addStudentProxyTile(student, false);

                    if ($stateParams.onLoadNotifications.length === 0) {
                        notificationCenterService.addNotification($filter('i18n')('proxy.error.accessExpired', [student.name]), $rootScope.notificationErrorType, true);
                    }
                });
            });

            $scope.appTiles.push(
                {
                    title: 'banner.generalssb.landingpage.personal.title',
                    desc: 'banner.generalssb.landingpage.personal.description',
                    url: 'proxyPersonalInfo'
                }
            );

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.proxyTiles = [];
        $scope.appTiles = [];
        $scope.isSingleTile;
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
