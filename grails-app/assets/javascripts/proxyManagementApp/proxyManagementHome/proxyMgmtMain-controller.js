/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtMainController',['$scope', '$rootScope', '$location', '$state', '$stateParams',
    '$timeout', '$filter', 'notificationCenterService', 'proxyMgmtAppService', 'proxyConfigResolve',
    function ($scope, $rootScope, $location, $state, $stateParams, $timeout, $filter, notificationCenterService, proxyMgmtAppService, proxyConfigResolve) {

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

        refreshProxies = function (response) {
            $scope.proxies = response.proxies;
            $scope.proxiesLoaded = true;
        },

        init = function() {
            proxyMgmtAppService.getProxyList().$promise.then(function (response) {
                refreshProxies(response)
            });

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.proxies = [];
        $scope.proxiesLoaded = false;

        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.confirmDeleteProxy = function (proxy) {
            var deleteProxy = function () {
                $scope.cancelNotification();

                proxyMgmtAppService.deleteProxy(proxy).$promise.then(function (response) {

                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        refreshProxies(response);
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('proxyManagement.label.button.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('proxyManagement.label.button.delete'),
                    action: deleteProxy
                }
            ];

            if ($scope.proxyCanBeDeleted(proxy)) {
                notificationCenterService.addNotification('proxyManagement.confirm.proxy.delete.text', 'warning', false, prompts);
            }
        };

        $scope.goToEditProxyState = function() {
            $state.go('editProxy');
        };

        $scope.enableDeleteRelationship = proxyConfigResolve.enableDeleteRelationship;
        // $scope.enableDeleteAfterDays = proxyConfigResolve.enableDeleteAfterDays;

        $scope.proxyCanBeDeleted = function(proxy){
            // var enableDeleteAfterDaysIsDisabled = $scope.enableDeleteAfterDays === 0 || $scope.enableDeleteAfterDays === null;
            // return ($scope.enableDeleteRelationship && (enableDeleteAfterDaysIsDisabled || $scope.enableDeleteAfterDays < proxy.daysFromLastView));
            return ($scope.enableDeleteRelationship && (proxy.deleteAllowedPerLastView === 'Y'));
        };

        // INITIALIZE
        // ----------
        init();

    }
]);
