/********************************************************************************
  Copyright 2020 Ellucian Company L.P. and its affiliates.
********************************************************************************/
globalProxyMgmtAppControllers.controller('globalProxyMgmtMainController',['$scope', '$rootScope', '$location', '$state', '$stateParams',
    '$timeout', '$filter', 'notificationCenterService', 'globalProxyMgmtAppService',
    function ($scope, $rootScope, $location, $state, $stateParams, $timeout, $filter, notificationCenterService, globalProxyMgmtAppService) {

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

            globalProxyMgmtAppService.getProxyList().$promise.then(function (response) {

                refreshProxies(response)

                //console.log(JSON.stringify(response));

                $rootScope.studenSSB = "http://gvutrans02.greatvalleyu.com:8080/StudentSelfService";


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

                _.each($scope.students.active, function(student) {
                    addStudentProxyTile(student, true);
                });


            });


            globalProxyMgmtAppService.getDoesUserHaveActivePreferredEmailAddress().$promise.then(function(response){
                $scope.userHasActivePreferredEmailAddress = response.doesUserHaveActivePreferredEmailAddress;
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

                globalProxyMgmtAppService.deleteProxy(proxy).$promise.then(function (response) {

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

        //$scope.enableDeleteRelationship = proxyConfigResolve.enableDeleteRelationship;

        $scope.proxyCanBeDeleted = function(proxy){
            return ($scope.enableDeleteRelationship && (proxy.deleteAllowedPerLastView === 'Y'));
        };

        // INITIALIZE
        // ----------

        // CONTROLLER VARIABLES
        // --------------------
        $scope.proxyTiles = [];
        $scope.appTiles = [];
        $scope.isSingleTile;
        $scope.pages =[];

        init();

    }
]);
