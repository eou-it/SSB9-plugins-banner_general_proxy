/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtEditProxyController', ['$scope', '$rootScope', '$state', '$location', '$stateParams',
    '$timeout', '$filter', '$q', 'notificationCenterService', 'proxyMgmtAppService', 'proxyMgmtErrorService', 'proxyConfigResolve',
    'proxyMgmtDateService', 'ProxyManagementProxy', 'ProxyManagementDataValidator',
    function ($scope, $rootScope, $state, $location, $stateParams, $timeout, $filter, $q, notificationCenterService,
              proxyMgmtAppService, proxyMgmtErrorService, proxyConfigResolve, proxyMgmtDateService, ProxyManagementProxy, ProxyManagementDataValidator) {

        // LOCAL FUNCTIONS
        // ---------------
        /**
         * Show any notifications slated to be shown on state load.
         * (The timeout is needed in cases where the common platform control bar needs time to load. It
         * may be that it's not a typical concern -- would only affect showing notifications on initial
         * page load -- but it's barely noticeable so doesn't hurt to leave it.)
         */
        var displayNotificationsOnStateLoad = function () {
                $timeout(function () {
                    _.each($stateParams.onLoadNotifications, function (notification) {
                        notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                    });
                }, 200);
            },
            setSelectedRelationship = function (code) {
                $scope.proxyAuxData.selectedRelationship = _.find($scope.relationshipChoices, function (rel) {
                    return rel.code == code;
                });

                if (!$scope.proxyAuxData.selectedRelationship) {
                    $scope.proxyAuxData.selectedRelationship = {code: null, description: null};
                }

                $scope.isRelationshipSelected = !!code;
            },
            displayResponseMessages = function (messages) {
                _.each(messages, function (message) {
                    if (message.code === 'PIN_EXPIRATION_DATE') {
                        $scope.passwordExpDateMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                    }

                    if (message.code === 'EMAIL_VERIFIED') {
                        $scope.emailVerifiedDateMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                    }

                    if (message.code === 'OPTOUT') {
                        $scope.optOutMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                    }
                    notificationCenterService.addNotification($filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]), $rootScope.notificationInfoType, true);
                });
            },
            displayResponseFailureMessages = function (flashMessage, notificationMessage) {
                $scope.flashMessage = flashMessage;
                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(notificationMessage, "error", true);
            },
            init = function () {
                var alt = $stateParams.alt,
                    cver;

                $scope.proxyAuxData.firstName = $stateParams.firstName;
                $scope.proxyAuxData.lastName = $stateParams.lastName;
                $scope.proxyAuxData.email = $stateParams.email;

                $scope.proxyManagementDataValidator = new ProxyManagementDataValidator();

                proxyMgmtAppService.getRelationshipOptions().$promise.then(function (response) {
                    if (response.failure) {
                        displayResponseFailureMessages(response.message, response.message);
                    } else {
                        $scope.relationshipChoices = response.relationships;
                    }
                });

                proxyMgmtErrorService.refreshProxyManagementDateErrorManager();

                if (alt) {
                    $scope.currentAlt = alt;
                    cver = $stateParams.cver;
                    $scope.currentCver = cver;

                    // Set up for "edit proxy"
                    $scope.isCreateNew = false;

                    $scope.isRelationshipSelected = true;

                    proxyMgmtAppService.getProxy({alt: alt, cver: cver}).$promise.then(function (response) {
                        var setupEditDialog = function () {
                            $scope.proxy = new ProxyManagementProxy(response.proxyProfile);
                            $scope.authPages = $scope.proxy.getAuthorizedPages();
                            displayResponseMessages(response.messages.messages);
                            setSelectedRelationship($scope.proxy.p_retp_code);

                            proxyMgmtAppService.getClonedProxiesList({
                                alt: alt,
                                cver: cver,
                                p_retp_code: $scope.proxy.p_retp_code
                            }).$promise.then(function (response) {
                                if (response.failure) {
                                    displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                                } else {
                                    $scope.clonedProxiesList = response.cloneList;
                                }
                            });

                            // watch changes on passphrase
                            $scope.$watch('proxy.p_passphrase', function (newVal, oldVal) {
                                $scope.dirty = (newVal != oldVal)
                            });
                            // watch changes on pages
                            $scope.$watch('proxy.pages | json', function (newVal, oldVal) {
                                $scope.dirty = !_.isEqual(newVal, oldVal);
                            });
                        };

                        if (response.failure) {
                            displayResponseFailureMessages(response.message, response.message);
                        } else {
                            setupEditDialog();
                        }
                    });
                } else {
                    $scope.proxy = new ProxyManagementProxy();

                    setSelectedRelationship($scope.proxy.p_retp_code);

                    proxyMgmtAppService.getClonedProxiesListOnCreate().$promise.then(function (response) {
                        if (response.failure) {
                            displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                        } else {
                            $scope.clonedProxiesList = response.cloneList;
                        }
                    });

                    proxyMgmtAppService.getAddProxiesList().$promise.then(function (response) {
                        if (response.failure) {
                            displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                        } else {
                            $scope.addProxiesList = response.addList;
                        }
                    });
                }

                displayNotificationsOnStateLoad();
            };

        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.setupSelectCtrlFocusser = function ($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.handleRelationshipChange = function () {
            proxyMgmtAppService.getDataModelOnRelationshipChange({
                alt: $scope.proxy.alt,
                cver: $scope.proxy.cver,
                p_retp_code: $scope.proxyAuxData.selectedRelationship.code
            }).$promise.then(function (response) {
                if (response.failure) {
                    displayResponseFailureMessages(response.message.clonedProxiesList, response.message);
                } else {
                    $scope.proxy.handleRelationshipChange(response, $scope.proxyAuxData);
                    $scope.isRelationshipSelected = !!$scope.proxyAuxData.selectedRelationship.code;
                    $scope.proxyManagementDataValidator.removeProxyProfileFieldErrors($scope.proxy);

                    if ($scope.proxy.pages.length == 0) {
                        notificationCenterService.clearNotifications();
                        notificationCenterService.addNotification('proxyManagement.message.noAuthorizationsAvailable', "error", true);
                    }
                }
            });
        };

        $scope.handleClonedListChange = function () {

            if ($scope.isCreateNew) {
                $scope.proxy.p_retp_code = $scope.proxyAuxData.clonedProxy.retp;
                setSelectedRelationship($scope.proxy.p_retp_code);
            }

            proxyMgmtAppService.getClonedAuthorizationsList({
                alt: $scope.proxyAuxData.clonedProxy.code,
                cver: $scope.proxyAuxData.clonedProxy.cver,
                p_retp_code: $scope.proxy.p_retp_code
            }).$promise.then(function (response) {
                if (response.failure) {
                    displayResponseFailureMessages(response.message, response.message);
                } else {
                    $scope.proxy.pages = response.pages;
                    $scope.proxyManagementDataValidator.removeProxyProfileFieldErrors($scope.proxy);
                }
            });

        };

        $scope.handleAddListChange = function () {
            $scope.proxy.handleAddListChange($scope.proxyAuxData);
        };

        $scope.emailPassphrase = function () {

            if (!$scope.proxy.p_passphrase) {
                notificationCenterService.addNotification('proxyManagement.profile.error.passphrase', "error", true);
            } else if ($scope.dirty) {
                notificationCenterService.addNotification('proxyManagement.message.checkDirtyOnEmail', 'error', true);
            } else {

                proxyMgmtAppService.emailPassphrase({alt: $scope.proxy.alt, cver: $scope.proxy.cver}).$promise.then(function (response) {
                    var messageType, message;

                    if (response.failure) {
                        messageType = 'error';
                        message = response.message;
                    } else {
                        if (response.resetStatus != 'SUCCESS') {
                            messageType = 'error';
                            message = 'proxyManagement.message.emailPassphraseFailure';
                        } else {
                            messageType = 'success';
                            message = 'proxyManagement.message.emailPassphraseSuccess';
                        }
                    }

                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification(message, messageType, true);
                });

            }
        };

        $scope.resetPassword = function () {
            proxyMgmtAppService.resetProxyPassword({alt: $scope.proxy.alt, cver: $scope.proxy.cver}).$promise.then(function (response) {
                var messageType, message;

                if (response.failure) {
                    messageType = 'error';
                    message = response.message;
                } else {
                    if (response.resetStatus == 'NOTACTIVE') {
                        messageType = 'error';
                        message = 'proxyManagement.message.resetPasswordFailure';
                    } else {
                        messageType = 'success';
                        message = 'proxyManagement.message.resetPasswordSuccess';
                    }
                }

                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(message, messageType, true);
            });
        };

        $scope.emailAuthentications = function () {

            if ($scope.dirty) {
                notificationCenterService.addNotification('proxyManagement.message.checkDirtyOnEmail', 'error', true);
                return;
            }

            proxyMgmtAppService.emailAuthentications({alt: $scope.proxy.alt, cver: $scope.proxy.cver}).$promise.then(function (response) {
                var messageType, message;

                if (response.failure) {
                    messageType = 'error';
                    message = response.message;
                } else {
                    if (response.resetStatus != 'SUCCESS') {
                        messageType = 'error';
                        message = 'proxyManagement.message.emailAuthorizationsFailure';
                    } else {
                        messageType = 'success';
                        message = 'proxyManagement.message.emailAuthorizationsSuccess';
                    }
                }

                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(message, messageType, true);
            });
        };

        //toggle all checkboxes
        $scope.toggleSelect = function () {
            $scope.proxy.toggleCheckboxes();
            $scope.proxyManagementDataValidator.removeProxyProfileFieldErrors($scope.proxy);
        };

        $scope.setStartFocused = function (focused) {
            $scope.startFocused = focused;
        };

        $scope.setStopFocused = function (focused) {
            $scope.stopFocused = focused;
        };

        $scope.$watch('proxy.p_start_date', function (newVal, oldVal) {
            if (newVal !== oldVal && !$scope.startFocused) {
                $scope.proxyManagementDataValidator.setStartAndStopDateErrors($scope.proxy);
            }
        });

        $scope.$watch('proxy.p_stop_date', function (newVal, oldVal) {
            if (newVal !== oldVal && !$scope.stopFocused) {
                $scope.proxyManagementDataValidator.setStartAndStopDateErrors($scope.proxy);
            }
        });

        $scope.setStartDate = function (data) {
            $scope.proxy.p_start_date = data;
            //$apply used to get binding to update when date chosen through datepicker.
            $scope.$apply(function () {
                $scope.proxyManagementDataValidator.setStartAndStopDateErrors($scope.proxy);
            })
        };

        $scope.setStopDate = function (data) {
            $scope.proxy.p_stop_date = data;
            //$apply used to get binding to update when date chosen through datepicker.
            $scope.$apply(function () {
                $scope.proxyManagementDataValidator.setStartAndStopDateErrors($scope.proxy);
            })
        };

        $scope.save = function () {

            var profile = {};

            //Add to Profile to manage dates
            _.each(Object.keys($scope.proxy), function (it) {
                profile[it] = $scope.proxy[it];
            });

            profile.p_start_date = proxyMgmtDateService.stringToDate($scope.proxy.p_start_date);
            profile.p_stop_date = proxyMgmtDateService.stringToDate($scope.proxy.p_stop_date);
            //End Profile Clone

            if ($scope.isCreateNew) { // CREATE PROXY
                if ($scope.proxyManagementDataValidator.isValidProxyData($scope.proxy)) {
                    notificationCenterService.addNotification('proxy.personalinformation.onSave.waitMessage', 'success', true);

                    proxyMgmtAppService.createProxy(profile).$promise.then(function (response) {
                        var notifications = [],
                            doStateGoSuccess = function (messageOnSave) {
                                notifications.push({
                                    message: messageOnSave ? messageOnSave : 'proxyManagement.label.createSuccess',
                                    messageType: $scope.notificationSuccessType,
                                    flashType: $scope.flashNotification
                                });

                                $state.go('home',
                                    {onLoadNotifications: notifications},
                                    {reload: true, inherit: false, notify: true}
                                );
                            };

                        if (response.failure) {
                            displayResponseFailureMessages(response.message, response.message);
                        } else {
                            doStateGoSuccess(response.message);
                        }
                    });
                } else {
                    proxyMgmtErrorService.displayMessages();
                }
            } else { // UPDATE PROXY
                if ($scope.proxyManagementDataValidator.isValidProxyData($scope.proxy, true)) {
                    notificationCenterService.addNotification('proxy.personalinformation.onSave.waitMessage', 'success', true);

                    proxyMgmtAppService.updateProxy(profile).$promise.then(function (response) {
                        var notifications = [],
                            doStateGoSuccess = function (messageOnSave) {
                                notifications.push({
                                    message: messageOnSave ? messageOnSave : 'proxyManagement.label.updateSuccess',
                                    messageType: $scope.notificationSuccessType,
                                    flashType: $scope.flashNotification
                                });

                                $state.go('home',
                                    {onLoadNotifications: notifications},
                                    {reload: true, inherit: false, notify: true}
                                );
                            };

                        if (response.failure) {
                            displayResponseFailureMessages(response.message, response.message);
                        } else {
                            doStateGoSuccess(response.message);
                        }
                    });
                } else {
                    proxyMgmtErrorService.displayMessages();
                }

            }
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
        $scope.proxy;
        $scope.proxyAuxData = {
            selectedRelationship: {code: null, description: null},
            firstName: null,
            lastName: null,
            email: null,
            clonedProxy: {code: null, description: null, retp: null},
            addProxy: {code: null, description: null, email: null, firstName: null, lastName: null}
        };

        $scope.placeholder = {
            first_name: $filter('i18n')('proxyManagement.placeholder.first_name'),
            last_name: $filter('i18n')('proxyManagement.placeholder.last_name'),
            email: $filter('i18n')('proxyManagement.placeholder.email'),
            verify_email: $filter('i18n')('proxyManagement.placeholder.verifyEmail'),
            relationship: $filter('i18n')('proxyManagement.placeholder.relationship'),
            desc: $filter('i18n')('proxyManagement.label.description'),
            passphrase: $filter('i18n')('proxyManagement.label.passphrase'),
            clonedLList: $filter('i18n')('proxyManagement.placeholder.selectPerson')
        };

        $scope.isRelationshipSelected = false;
        $scope.relationshipChoices = [];
        $scope.clonedProxiesList = [];
        $scope.addProxiesList = [];
        $scope.passwordExpDateMsg = '';
        $scope.emailVerifiedDateMsg = '';
        $scope.optOutMsg = '';
        $scope.authPages = [];
        $scope.startFocused = false;
        $scope.stopFocused = false;
        $scope.proxyManagementDataValidator = {};

        $scope.maxNameLength = 60;
        $scope.maxEmailLength = 128;
        $scope.maxPassphraseLength = 256;
        $scope.maxDescriptionLength = 120;

        // COMMUNICATION DATA TABLE
        // ------------------------
        $scope.commRecords = 0;
        $scope.commRows = [];
        $scope.commColumns = [
            {position: {desktop: 1, mobile: 1}, name: 'transmitDate', title: $filter('i18n')('proxyManagement.title.transmitDate'), options: {visible: true, sortable: false}, width: '60px'},
            {position: {desktop: 2, mobile: 2}, name: 'subject', title: $filter('i18n')('proxyManagement.title.subject'), options: {visible: true, sortable: false}, width: '100px'},
            {position: {desktop: 3, mobile: 3}, name: 'actionDate', title: $filter('i18n')('proxyManagement.title.actionDate'), options: {visible: true, sortable: false}, width: '60px'},
            {position: {desktop: 4, mobile: 4}, name: 'expirationDate', title: $filter('i18n')('proxyManagement.title.expirationDate'), options: {visible: true, sortable: false}, width: '60px'},
            {position: {desktop: 5, mobile: 5}, name: 'resend', title: $filter('i18n')('proxyManagement.title.resend'), options: {visible: true, sortable: false}, width: '25px'}
        ];
        $scope.draggableCommColumnNames = [];

        $scope.authLogRecords = 0;
        $scope.authLogRows = [];
        $scope.authLogColumns = [
            {position: {desktop: 1, mobile: 1}, name: 'activityDate', title: $filter('i18n')('proxyManagement.title.date'), options: {visible: true, sortable: false}, width: '100px'},
            {position: {desktop: 2, mobile: 2}, name: 'action', title: $filter('i18n')('proxyManagement.title.action'), options: {visible: true, sortable: false}, width: '60px'},
            {position: {desktop: 3, mobile: 3}, name: 'page', title: $filter('i18n')('proxyManagement.title.page'), options: {visible: true, sortable: false}, width: '100px'}
        ];
        $scope.draggableAuthLogColumnNames = [];

        $scope.currentAlt;
        $scope.currentCver;

        $scope.getCommunicationData = function (query) {
            var self = this;

            return proxyMgmtAppService.getCommunicationLog({alt: self.currentAlt, cver: self.currentCver}).promise;
        };

        $scope.getAuthLogData = function (query) {
            var self = this;

            return proxyMgmtAppService.getAuthLog({alt: self.currentAlt, cver: self.currentCver}).$promise;
        };

        $scope.commMobileConfig = {
            transmitDate: 2,
            subject: 2,
            actionDate: 2,
            expirationDate: 2,
            resend: 2
        };

        $scope.commPaginationConfig = {};

        $scope.authLogMobileConfig = {
            activityDate: 2,
            action: 2,
            page: 2
        };

        $scope.authLogPaginationConfig = {};

        $scope.enableResetPin = proxyConfigResolve.enableResetPin;
        $scope.enablePassphrase = proxyConfigResolve.enablePassphrase;
        $scope.enablePageLevelAuthorization = proxyConfigResolve.enablePageLevelAuthorization;
        $scope.enableTabCommunication = proxyConfigResolve.enableTabCommunication;
        $scope.enableTabHistory = proxyConfigResolve.enableTabHistory;
        // ------------------------------
        // END - COMMUNICATION DATA TABLE

        // INITIALIZE
        // ----------
        init();

    }
]);
