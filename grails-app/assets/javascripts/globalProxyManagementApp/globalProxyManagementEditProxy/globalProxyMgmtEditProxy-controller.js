/********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
globalProxyMgmtAppControllers.controller('globalProxyMgmtEditProxyController', ['$scope', '$rootScope', '$state', '$location', '$stateParams',
    '$timeout', '$filter', '$q', 'notificationCenterService', 'globalProxyMgmtAppService', 'globalProxyMgmtErrorService', 'GlobalProxyManagementProxy', 'GlobalProxyManagementDataValidator',
    function ($scope, $rootScope, $state, $location, $stateParams, $timeout, $filter, $q, notificationCenterService,
              globalProxyMgmtAppService, globalProxyMgmtErrorService, GlobalProxyManagementProxy, GlobalProxyManagementDataValidator) {

        // LOCAL FUNCTIONS
        // ---------------
        /**
         * Show any notifications slated to be shown on state load.
         * (The timeout is needed in cases where the common platform control bar needs time to load. It
         * may be that it's not a typical concern -- would only affect showing notifications on initial
         * page load -- but it's barely noticeable so doesn't hurt to leave it.)
         */
        let displayNotificationsOnStateLoad = function () {
                $timeout(function () {
                    _.each($stateParams.onLoadNotifications, function (notification) {
                        notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                    });
                }, 200);
            },
            displayResponseFailureMessages = function (flashMessage, notificationMessage) {
                $scope.flashMessage = flashMessage;
                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(notificationMessage, "error", true);
            },
            beforeUpdateProxy,
            beforeUpdateProxyAuxData,
            formDirty = false,
            showSaveCancelMessage = function (toState) {
                let prompt = [
                    {
                        label: $filter('i18n')('proxy.label.cancel'),
                        action: function () {
                            notificationCenterService.removeNotification('globalProxyManagement.message.saveCancel');
                        }
                    },
                    {
                        label: $filter('i18n')('globalProxyManagement.message.no'),
                        action: function () {
                            notificationCenterService.removeNotification('globalProxyManagement.message.saveCancel');
                            formDirty = false;
                            $state.go(toState)
                        }
                    },
                    {
                        label: $filter('i18n')('globalProxyManagement.message.yes'),
                        action: function () {
                            notificationCenterService.removeNotification('globalProxyManagement.message.saveCancel');

                            //Attempt to save the target.
                            $scope.save()
                        }
                    }
                ];
                notificationCenterService.displayNotification('globalProxyManagement.message.saveCancel', 'warning', false, prompt);
            },
            init = function () {
                $scope.globalProxyManagementDataValidator = new GlobalProxyManagementDataValidator();

                globalProxyMgmtAppService.getRelationshipOptions().$promise.then(function (response) {
                    if (response.failure) {
                        displayResponseFailureMessages(response.message, response.message);
                    } else {
                        if (response.relationships) {
                            if (response.relationships.length > 0) {
                                $scope.relationshipChoices = response.relationships;
                            } else {
                                notificationCenterService.addNotification($filter('i18n')('globalProxyManagement.message.noRelationships'), "error", true);
                            }
                        }
                    }
                });

                beforeUpdateProxy = angular.copy($scope.proxy);
                beforeUpdateProxyAuxData = angular.copy($scope.proxyAuxData);

                displayNotificationsOnStateLoad();
            },
            setPreferredName = function (preferredName) {
                if (preferredName) {
                    $scope.proxy.preferredName = preferredName;
                } else {
                    $scope.proxy.preferredName = ''
                }
            }
        ;

        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.setupSelectCtrlFocusser = function ($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.handleRelationshipChange = function () {
            globalProxyMgmtAppService.getDataModelOnRelationshipChange({
                p_retp_code: $scope.proxyAuxData.selectedRelationship.code
            }).$promise.then(function (response) {
                if (response.failure) {
                    displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                } else {
                    notificationCenterService.clearNotifications();
                    $scope.proxy.handleRelationshipChange(response, $scope.proxyAuxData);
                    $scope.isRelationshipSelected = !!$scope.proxyAuxData.selectedRelationship.code;
                    $scope.globalProxyManagementDataValidator.removeProxyProfileFieldErrors($scope.proxy);
                    $scope.globalProxyManagementDataValidator.isValidProxyData($scope.proxy, false);
                    globalProxyMgmtErrorService.displayMessages();
                }
            });
        };

        $scope.handleBannerIdChange = function (proxy) {
            notificationCenterService.clearNotifications();
            globalProxyMgmtAppService.isGlobalProxyAccessTargetValid({targetId: proxy.targetId}).$promise.then(function (response) {
                if (response.failure) {
                    displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                } else {
                    $scope.proxy.isValidBannerId = response.isValidBannerId;
                    $scope.proxy.isValidTarget = response.isValidToBeProxied;
                    setPreferredName(response.preferredName);
                    if (!$scope.globalProxyManagementDataValidator.isValidProxyData($scope.proxy, false)) {
                        globalProxyMgmtErrorService.displayMessages();
                    }
                }
            });
        };

        $scope.save = function () {
            notificationCenterService.clearNotifications();

            globalProxyMgmtAppService.isGlobalProxyAccessTargetValid({targetId: $scope.proxy.targetId}).$promise.then(function (response) {
                if (response.failure) {
                    displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                } else {
                    $scope.proxy.isValidBannerId = response.isValidBannerId;
                    $scope.proxy.isValidTarget = response.isValidToBeProxied;

                    setPreferredName(response.preferredName);

                    if ($scope.globalProxyManagementDataValidator.isValidProxyData($scope.proxy, true)) {
                        globalProxyMgmtAppService.createProxy({retp: $scope.proxy.p_retp_code, targetBannerId: $scope.proxy.targetId}).$promise.then(function (response) {
                            if (response.failure) {
                                notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                            } else {
                                const notifications = [],
                                    doStateGoSuccess = function () {
                                        formDirty = false;

                                        notifications.push({
                                            message: 'globalProxyManagement.create.success',
                                            messageType: $scope.notificationSuccessType,
                                            flashType: $scope.flashNotification
                                        });

                                        $state.go('home',
                                            {onLoadNotifications: notifications},
                                            {reload: true, inherit: false, notify: true}
                                        );
                                    };
                                doStateGoSuccess();
                            }
                        });
                    } else {
                        globalProxyMgmtErrorService.displayMessages();
                    }
                }
            });

        };

        $scope.cancel = function () {
            $state.go('home',
                {reload: true, inherit: false, notify: true}
            );
        };

        // CONTROLLER VARIABLES
        // --------------------
        $scope.dirty = false;
        $scope.isCreateNew = true;
        $scope.proxy = new GlobalProxyManagementProxy();
        $scope.isTargetIdValid = false;
        $scope.proxyAuxData = {
            selectedRelationship: {code: null},
            clonedProxy: {code: null, retp: null},
            addProxy: {code: null, description: null}
        };

        $scope.placeholder = {
            relationship: $filter('i18n')('proxyManagement.placeholder.relationship'),
            clonedLList: $filter('i18n')('proxyManagement.placeholder.selectPerson')
        };

        $scope.isRelationshipSelected = false;
        $scope.isBannerIdFocused = false;
        $scope.relationshipChoices = [];
        $scope.clonedProxiesList = [];
        $scope.globalProxyManagementDataValidator = {};

        $scope.maxBannerIdLength = 30;

        let isOnIOS = navigator.userAgent.match(/iPad|iPhone|iPod/i);
        let eventName = isOnIOS ? "pagehide" : "beforeunload";
        $(window).on(eventName, function () {
            if (formDirty) {
                return $filter('i18n')('globalProxyManagement.message.saveCancel')
            }
        });

        let $stateChangeStartUnbind = $scope.$on('$stateChangeStart', function (event, toState) {
            if (formDirty) {
                debugger;
                event.preventDefault();
                showSaveCancelMessage(toState)
            }
        });

        $scope.$watch('proxy', function (newVal) {
            if (formDirty !== true) {
                formDirty = !angular.equals(newVal, beforeUpdateProxy);
            }
        }, true);

        $scope.$watch('proxyAuxData', function (newVal) {
            if (formDirty !== true) {
                formDirty = !angular.equals(newVal, beforeUpdateProxyAuxData);
            }
        }, true);

        // INITIALIZE
        // ----------
        init();

        $scope.$on('$destroy', function () {
            $(window).off(eventName);
            $stateChangeStartUnbind();
        });

    }
]);
