/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtMainController',['$scope', '$rootScope', '$location', '$stateParams', '$timeout',
    '$filter', 'notificationCenterService', 'proxyMgmtAppService',
    function ($scope, $rootScope, $location, $stateParams, $timeout, $filter, notificationCenterService, proxyMgmtAppService) {

        // LOCAL FUNCTIONS
        // ---------------
        /**
         * Show any notifications slated to be shown on state load.
         * (The timeout is needed in cases where the common platform control bar needs time to load. It
         * may be that it's not a typical concern -- would only affect showing notifications on initial
         * page load -- but it's barely noticeable so doesn't hurt to leave it.)
         */
        var displayNotificationsOnStateLoad = function() {
            $timeout(function() {
                _.each($stateParams.onLoadNotifications, function(notification) {
                    notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                });
            }, 200);
        },

        init = function() {
            proxyMgmtAppService.getProxyList().$promise.then(function (response) {
                $scope.proxies = response.proxies;

            });

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.proxies = [];


        // INITIALIZE
        // ----------
        init();

    }
]);
