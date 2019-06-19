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

                // TODO: REMOVE ONCE NO LONGER NEEDED
                if (confirm('FOR TESTING PURPOSES: Show existing proxies?')) {
                    $scope.proxies = mockProxyData.proxies;
                }
            });

            displayNotificationsOnStateLoad();
        };

        // TODO: TEMPORARY DATA - REMOVE ONCE NO LONGER NEEDED
        var mockProxyData = {
            "proxies":
                [
                    {
                        "gidm": "-99999627",
                        "firstName": "French",
                        "lastName": "Horne",
                        "email": "daren.dunn@ellucian.com"
                    },
                    {
                        "gidm": "-99998824",
                        "firstName": "Hoot",
                        "lastName": "Owl",
                        "email": "improxy86@gmail.com"
                    },
                    {
                        "gidm": "-99998822",
                        "firstName": "Night",
                        "lastName": "Owl",
                        "email": "improxy85@gmail.com"
                    },
                    {
                        "gidm": "-99998880",
                        "firstName": "Robin",
                        "lastName": "Red",
                        "email": "improxy84@gmail.com"
                    },
                    {
                        "gidm": "-99999695",
                        "firstName": "Sue",
                        "lastName": "Sarasue",
                        "email": "improxy24@gmail.com"
                    }
                ]
        };
        // END TEMPORARY DATA


        // CONTROLLER VARIABLES
        // --------------------
        $scope.proxies = [];


        // INITIALIZE
        // ----------
        init();

    }
]);
