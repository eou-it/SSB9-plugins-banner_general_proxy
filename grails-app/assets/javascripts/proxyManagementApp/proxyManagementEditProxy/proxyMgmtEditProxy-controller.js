/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtEditProxyController',['$scope', '$rootScope', '$state','$location', '$stateParams', '$timeout',
    '$filter', 'notificationCenterService', 'proxyMgmtAppService',
    function ($scope, $rootScope, $state,$location, $stateParams, $timeout, $filter, notificationCenterService, proxyMgmtAppService) {

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
            $scope.proxyAuxData.selectedRelationship = $scope.relationshipChoices.find(function (rel) {
                return rel.code == code;
            });

            if (!$scope.proxyAuxData.selectedRelationship) {
                $scope.proxyAuxData.selectedRelationship = {code: null, description: null};
            }

            $scope.isRelationshipSelected = !!code;
        },

        init = function() {
            var gidm = $stateParams.gidm;

            $scope.proxyAuxData.firstName = $stateParams.firstName;
            $scope.proxyAuxData.lastName = $stateParams.lastName;
            $scope.proxyAuxData.email = $stateParams.email;

            if (gidm) {
                // Set up for "edit proxy"
                $scope.isCreateNew = false;

                $scope.isRelationshipSelected = true;

                proxyMgmtAppService.getProxy({gidm: gidm}).$promise.then(function (response) {
                    $scope.proxy = response.proxyProfile;

                    setSelectedRelationship($scope.proxy.p_retp_code);
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
                    pages: [
                        {url:"/ssb/proxy/holds", desc: "Student Holds", auth: false},
                        {url:"/ssb/proxy/grades", desc: "Midterm and Final Grades", auth: false}
                    ]
                };

                setSelectedRelationship($scope.proxy.p_retp_code);
            }

            displayNotificationsOnStateLoad();
        };


        $scope.setupSelectCtrlFocusser = function($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.handleRelationshipChange = function() {
            proxyMgmtAppService.getDataModelOnRelationshipChange({gidm: $scope.proxy.gidm, p_retp_code: $scope.proxyAuxData.selectedRelationship.code}).$promise.then(function (response) {
                $scope.proxy.p_start_date = response.dates.startDate;
                $scope.proxy.p_stop_date = response.dates.stopDate;
                $scope.proxy.pages = response.pages.pages;
            });
        };

        $scope.emailPassphrase = function() {

            if  (!$scope.proxy.p_passphrase){

            notificationCenterService.addNotification('proxyManagement.profile.error.passphrase', "error", true);

            }else{
                //TO DO
            }
            };


        $scope.resetPin = function() {
            //TO DO
        };


        $scope.emailAuthentications = function() {
            //TO DO
        };

        //toggle all checkboxes
        $scope.toggleSelect = function(){

            $scope.proxy.pages.forEach(function (page) {
                    page.auth = event.target.checked;
                });

        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.proxy;
        $scope.proxyAuxData = {
            selectedRelationship: {code: null, description: null},
            firstName: null,
            lastName: null,
            email: null
        };

        $scope.placeholder = {
            first_name:   $filter('i18n')('proxy.management.placeholder.first_name'),
            last_name:    $filter('i18n')('proxy.management.placeholder.last_name'),
            email:        $filter('i18n')('proxy.management.placeholder.email'),
            verify_email: $filter('i18n')('proxy.management.placeholder.verifyEmail'),
            relationship: $filter('i18n')('proxy.management.placeholder.relationship'),
            desc:         $filter('i18n')('proxy.management.label.description'),
            passphrase:   $filter('i18n')('proxy.management.label.passphrase')
        };
        $scope.isRelationshipSelected = false;
        $scope.relationshipChoices = [
            // TODO: BELOW CHOICES ARE PLACEHOLDERS. IMPLEMENT DYNAMICALLY WITH I18N FILTER, ALONG THE LINES OF EXAMPLE BELOW:
            // {code: 'P', description: $filter('i18n')('proxy.personalinformation.label.parent')},
            {code: 'PARENT', description: 'Parent'},
            {code: 'EMPLOYER', description: 'Employer'},
            {code: 'ADVISOR', description: 'Advisor'},
            {code: 'SIBLING', description: 'Sibling'}
        ];

        $scope.setStartDate = function(data){
            $scope.proxy.p_start_date = data;
        };

        $scope.setStopDate = function(data){
            $scope.proxy.p_stop_date = data;
        };

        $scope.save = function() {
            notificationCenterService.addNotification('proxy.personalinformation.onSave.waitMessage', 'success', true);

            if ($scope.isCreateNew) {
                proxyMgmtAppService.createProxy($scope.proxy).$promise.then(function (response) {
                    var notifications = [],
                        doStateGoSuccess = function (messageOnSave) {
                            notifications.push({
                                message: messageOnSave ? messageOnSave : 'proxyManagement.label.saveSuccess',
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
            }else{

                proxyMgmtAppService.updateProxy($scope.proxy).$promise.then(function (response) {
                    var notifications = [],
                        doStateGoSuccess = function (messageOnSave) {
                            notifications.push({
                                message: messageOnSave ? messageOnSave : 'proxyManagement.label.saveSuccess',
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

            }
        };

        $scope.cancel = function() {
                $state.go('home',
                    {reload: true, inherit: false, notify: true}
                );
        };

        // INITIALIZE
        // ----------
        init();

    }
]);
