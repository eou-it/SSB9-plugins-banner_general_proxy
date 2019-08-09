/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtEditProxyController',['$scope', '$rootScope', '$state','$location', '$stateParams',
    '$timeout', '$filter', '$q', 'notificationCenterService', 'proxyMgmtAppService', 'proxyMgmtErrorService', 'proxyConfigResolve',
    function ($scope, $rootScope, $state,$location, $stateParams, $timeout, $filter, $q, notificationCenterService,
              proxyMgmtAppService, proxyMgmtErrorService, proxyConfigResolve) {

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

        setSelectedRelationship = function(code) {
            $scope.proxyAuxData.selectedRelationship = _.find($scope.relationshipChoices, function (rel) {
                return rel.code == code;
            });

            if (!$scope.proxyAuxData.selectedRelationship) {
                $scope.proxyAuxData.selectedRelationship = {code: null, description: null};
            }

            $scope.isRelationshipSelected = !!code;
        },

        isValidProxyData = function (proxy, isUpdate) {
            if (isUpdate) {
                // The corresponding fields for these errors are always empty for an update, so shim the error messages.
                $scope.firstNameErrMsg = false;
                $scope.lastNameErrMsg = false;
                $scope.emailErrMsg = false;
                $scope.verifyEmailErrMsg = false;
            } else {
                $scope.firstNameErrMsg = proxyMgmtErrorService.getErrorFirstName(proxy);
                $scope.lastNameErrMsg = proxyMgmtErrorService.getErrorLastName(proxy);
                $scope.emailErrMsg = proxyMgmtErrorService.getErrorEmail(proxy);
                $scope.verifyEmailErrMsg = proxyMgmtErrorService.getErrorVerifyEmail(proxy);
            }

            $scope.relationshipErrMsg =   proxyMgmtErrorService.getErrorRelationship(proxy);
            $scope.authorizationsErrMsg = proxyMgmtErrorService.getErrorAuthorizations(proxy);
            $scope.checkDatesErrMsg = proxyMgmtErrorService.getErrorDates(proxy);

            return !($scope.firstNameErrMsg || $scope.lastNameErrMsg || $scope.emailErrMsg || $scope.verifyEmailErrMsg ||
                     $scope.relationshipErrMsg || $scope.authorizationsErrMsg || $scope.checkDatesErrMsg);
        },

        init = function() {
            var alt = $stateParams.alt;

            $scope.proxyAuxData.firstName = $stateParams.firstName;
            $scope.proxyAuxData.lastName = $stateParams.lastName;
            $scope.proxyAuxData.email = $stateParams.email;

            proxyMgmtAppService.getRelationshipOptions().$promise.then(function(response) {
                if (response.failure) {
                    $scope.flashMessage = response.message;

                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification(response.message, "error", true);

                } else {
                    $scope.relationshipChoices = response.relationships;
                }
            });

            if (alt) {
                $scope.currentAlt = alt;

                // Set up for "edit proxy"
                $scope.isCreateNew = false;

                $scope.isRelationshipSelected = true;

                proxyMgmtAppService.getProxy({alt: alt}).$promise.then(function (response) {
                    $scope.proxy = response.proxyProfile;

                    _.each(response.messages.messages, function(message) {
                        if (message.code === 'PIN_EXPIRATION_DATE'){
                            $scope.passwordExpDateMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                        }

                        if (message.code === 'EMAIL_VERIFIED'){
                            $scope.emailVerifiedDateMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                        }

                        if (message.code === 'OPTOUT'){
                            $scope.optOutMsg = $filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]);
                        }
                        notificationCenterService.addNotification($filter('i18n')('proxyManagement.profile.label.' + message.code, [message.value]), $rootScope.notificationInfoType, true);
                    });

                    setSelectedRelationship($scope.proxy.p_retp_code);

                    proxyMgmtAppService.getClonedProxiesList({alt: alt, p_retp_code: $scope.proxy.p_retp_code}).$promise.then(function(response) {
                        if (response.failure) {
                            $scope.flashMessage = response.message.clonedProxiesList;

                            notificationCenterService.clearNotifications();
                            notificationCenterService.addNotification(response.message, "error", true);

                        } else {
                            $scope.clonedProxiesList = response.cloneList;
                        }
                    });

                });
            } else {
                // Create "new proxy" object
                $scope.proxy = {
                    p_email: null,
                    p_email_verify: null,
                    p_last: null,
                    p_first: null,
                    p_desc: null,
                    p_passphrase: null,
                    p_retp_code: null,
                    p_start_date: null,
                    p_stop_date: null,
                    pages: []
                };

                setSelectedRelationship($scope.proxy.p_retp_code);


                proxyMgmtAppService.getClonedProxiesListOnCreate({alt: alt}).$promise.then(function(response) {
                    if (response.failure) {
                        $scope.flashMessage = response.message.clonedProxiesList;

                        notificationCenterService.clearNotifications();
                        notificationCenterService.addNotification(response.message, "error", true);

                    } else {
                        $scope.clonedProxiesList = response.cloneList;
                    }
                });


                proxyMgmtAppService.getAddProxiesList({alt: alt}).$promise.then(function(response) {
                    if (response.failure) {
                        $scope.flashMessage = response.message.clonedProxiesList;

                        notificationCenterService.clearNotifications();
                        notificationCenterService.addNotification(response.message, "error", true);

                    } else {
                        $scope.addProxiesList = response.addList;
                    }
                });
            }

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.setupSelectCtrlFocusser = function($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.handleRelationshipChange = function() {
            proxyMgmtAppService.getDataModelOnRelationshipChange({alt: $scope.proxy.alt, p_retp_code: $scope.proxyAuxData.selectedRelationship.code}).$promise.then(function (response) {
                $scope.proxy.p_start_date = response.dates.startDate;
                $scope.proxy.p_stop_date = response.dates.stopDate;
                $scope.proxy.pages = response.pages.pages;
                $scope.proxy.p_retp_code = $scope.proxyAuxData.selectedRelationship.code;

                $scope.isRelationshipSelected = !!$scope.proxyAuxData.selectedRelationship.code;

                $scope.removeProxyProfileFieldErrors();


                if ($scope.proxy.pages.length == 0){
                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification('proxyManagement.message.noAuthorizationsAvailable', "error", true);
                }
            });
        };


        $scope.handleClonedListChange = function(){

            if ($scope.isCreateNew) {
                $scope.proxy.p_retp_code = $scope.proxyAuxData.clonedProxy.retp;
                setSelectedRelationship($scope.proxy.p_retp_code);
            }

            proxyMgmtAppService.getClonedAuthorizationsList({alt: $scope.proxyAuxData.clonedProxy.code, p_retp_code: $scope.proxy.p_retp_code}).$promise.then(function (response) {
                $scope.proxy.pages = response.pages;
            });

        };


        $scope.handleAddListChange = function(){

            $scope.proxy.p_email = $scope.proxyAuxData.addProxy.email;
            $scope.proxy.p_last = $scope.proxyAuxData.addProxy.lastName;
            $scope.proxy.p_first = $scope.proxyAuxData.addProxy.firstName;

        };


        $scope.emailPassphrase = function() {

            if  (!$scope.proxy.p_passphrase){

            notificationCenterService.addNotification('proxyManagement.profile.error.passphrase', "error", true);

            }else{

                proxyMgmtAppService.emailPassphrase({alt: $scope.proxy.alt}).$promise.then(function (response) {
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


        $scope.resetPassword = function() {
            proxyMgmtAppService.resetProxyPassword({alt: $scope.proxy.alt}).$promise.then(function (response) {
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

        $scope.emailAuthentications = function() {
            proxyMgmtAppService.emailAuthentications({alt: $scope.proxy.alt}).$promise.then(function (response) {
                var messageType, message;

                if (response.failure) {
                    messageType = 'error';
                    message = response.message;
                } else {
                    if (response.resetStatus != 'SUCCESS') {
                        messageType = 'error';
                        message = 'proxyManagement.message.resendAuthorizationsFailure';
                    } else {
                        messageType = 'success';
                        message = 'proxyManagement.message.resendAuthorizationsSuccess';
                    }
                }

                notificationCenterService.clearNotifications();
                notificationCenterService.addNotification(message, messageType, true);
            });
        };

        //toggle all checkboxes
        $scope.toggleSelect = function(){

            $scope.proxy.pages.forEach(function (page) {
                    page.auth = event.target.checked;
                });

            $scope.removeProxyProfileFieldErrors();
        };

        $scope.removeProxyProfileFieldErrors = function() {
            if($scope.firstNameErrMsg) {
                $scope.firstNameErrMsg = proxyMgmtErrorService.getErrorFirstName($scope.proxy);
            }
            if($scope.lastNameErrMsg) {
                $scope.lastNameErrMsg = proxyMgmtErrorService.getErrorLastName($scope.proxy);
            }
            if($scope.emailErrMsg) {
                $scope.emailErrMsg = proxyMgmtErrorService.getErrorEmail($scope.proxy);
            }
            if($scope.verifyEmailErrMsg) {
                $scope.verifyEmailErrMsg = proxyMgmtErrorService.getErrorVerifyEmail($scope.proxy);
            }
            if($scope.relationshipErrMsg) {
                $scope.relationshipErrMsg = proxyMgmtErrorService.getErrorRelationship($scope.proxy);
            }
            if($scope.authorizationsErrMsg) {
                $scope.authorizationsErrMsg = proxyMgmtErrorService.getErrorAuthorizations($scope.proxy);
            }
        };

        $scope.setStartDate = function(data){
            $scope.proxy.p_start_date = data;
        };

        $scope.setStopDate = function(data){
            $scope.proxy.p_stop_date = data;
        };

        $scope.save = function() {

            var profile = {};

            //Add to Profile to manage dates
            _.each(Object.keys($scope.proxy), function(it) {
                profile[it] = $scope.proxy[it];
            });

            profile.p_start_date = proxyMgmtAppService.stringToDate($scope.proxy.p_start_date);
            profile.p_stop_date = proxyMgmtAppService.stringToDate($scope.proxy.p_stop_date);
            //End Profile Clone

            if ($scope.isCreateNew) { // CREATE PROXY
                if (isValidProxyData($scope.proxy)) {
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
                            $scope.flashMessage = response.message;

                            notificationCenterService.clearNotifications();
                            notificationCenterService.addNotification(response.message, "error", true);

                        } else {
                            doStateGoSuccess(response.message);
                        }
                    });
                } else {
                    proxyMgmtErrorService.displayMessages();
                }
            }else{ // UPDATE PROXY
                if (isValidProxyData($scope.proxy, true)) {
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
                            $scope.flashMessage = response.message;

                            notificationCenterService.clearNotifications();
                            notificationCenterService.addNotification(response.message, "error", true);

                        } else {
                            doStateGoSuccess(response.message);
                        }
                    });
                } else {
                    proxyMgmtErrorService.displayMessages();
                }

            }
        };

        $scope.cancel = function() {
            $state.go('home',
                {reload: true, inherit: false, notify: true}
            );
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.proxy;
        $scope.proxyAuxData = {
            selectedRelationship: {code: null, description: null},
            firstName: null,
            lastName: null,
            email: null,
            clonedProxy: {code: null, description: null, retp : null},
            addProxy: {code: null, description: null, email : null, firstName :null, lastName : null}
        };

        $scope.placeholder = {
            first_name:   $filter('i18n')('proxyManagement.placeholder.first_name'),
            last_name:    $filter('i18n')('proxyManagement.placeholder.last_name'),
            email:        $filter('i18n')('proxyManagement.placeholder.email'),
            verify_email: $filter('i18n')('proxyManagement.placeholder.verifyEmail'),
            relationship: $filter('i18n')('proxyManagement.placeholder.relationship'),
            desc:         $filter('i18n')('proxyManagement.label.description'),
            passphrase:   $filter('i18n')('proxyManagement.label.passphrase'),
            clonedLList:  $filter('i18n')('proxyManagement.placeholder.selectPerson')
        };

        $scope.isRelationshipSelected = false;
        $scope.relationshipChoices = [];
        $scope.clonedProxiesList = [];
        $scope.addProxiesList = [];
        $scope.firstNameErrMsg = '';
        $scope.lastNameErrMsg = '';
        $scope.emailErrMsg = '';
        $scope.verifyEmailErrMsg = '';
        $scope.relationshipErrMsg = '';
        $scope.authorizationsErrMsg = '';
        $scope.checkDatesErrMsg = '';
        $scope.passwordExpDateMsg = '';
        $scope.emailVerifiedDateMsg = '';
        $scope.optOutMsg  = '';


        // COMMUNICATION DATA TABLE
        // ------------------------
        $scope.commRecords = 0;
        $scope.commRows = [];
        $scope.commColumns = [
            {position: {desktop: 1, mobile: 1}, name: 'transmitDate', title: $filter('i18n')('proxyManagement.title.transmitDate'), options: {visible: true, sortable:false}, width:'60px'},
            {position: {desktop: 2, mobile: 2}, name: 'subject', title: $filter('i18n')('proxyManagement.title.subject'), options: {visible: true, sortable:false}, width:'100px'},
            {position: {desktop: 3, mobile: 3}, name: 'actionDate', title: $filter('i18n')('proxyManagement.title.actionDate'), options: {visible: true, sortable:false}, width:'60px'},
            {position: {desktop: 4, mobile: 4}, name: 'expirationDate', title: $filter('i18n')('proxyManagement.title.expirationDate'), options: {visible: true, sortable:false}, width:'60px'},
            {position: {desktop: 5, mobile: 5}, name: 'resend', title: $filter('i18n')('proxyManagement.title.resend'), options: {visible: true, sortable:false}, width:'25px'}
        ];

        $scope.draggableCommColumnNames = [];
        $scope.currentAlt;

        $scope.getCommunicationData = function(query) {
            var self = this;

            return proxyMgmtAppService.getCommunicationLog({alt: self.currentAlt}).promise;
        };

        $scope.commMobileConfig = {
            transmitDate: 2,
            subject: 2,
            actionDate: 2,
            expirationDate: 2,
            resend: 2
        };

        $scope.commPaginationConfig = {

        };

        $scope.enableResetPin = proxyConfigResolve.enableResetPin;
        $scope.enablePassphrase = proxyConfigResolve.enablePassphrase;
        $scope.enablePageLevelAuthorization = proxyConfigResolve.enablePageLevelAuthorization;
        $scope.enableTabCommunication = proxyConfigResolve.enableTabCommunication;
        // ------------------------------
        // END - COMMUNICATION DATA TABLE


        // INITIALIZE
        // ----------
        init();

    }
]);
