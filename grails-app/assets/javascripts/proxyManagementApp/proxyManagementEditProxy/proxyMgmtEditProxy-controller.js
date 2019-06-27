/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
proxyMgmtAppControllers.controller('proxyMgmtEditProxyController',['$scope', '$rootScope', '$location', '$stateParams', '$timeout',
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
            var gidm = $stateParams.gidm;

            if (gidm) {
                // Set up for "edit proxy"
                $scope.isCreateNew = false;

                proxyMgmtAppService.getProxy({gidm: gidm}).$promise.then(function (response) {
                    $scope.proxy = response.proxy;

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
                    pages: null
                };

            }

            displayNotificationsOnStateLoad();
        };


        $scope.setupSelectCtrlFocusser = function($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.updateDateRange = function() {
            proxyMgmtAppService.getProxyStartStopDates({relationshipCode: 'A'}).$promise.then(function (response) {
                $scope.proxy.p_start_date = response.startDate;
                $scope.proxy.p_stop_date = response.stopDate;
            });
        };



        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.proxy;
        $scope.placeholder = {
            first_name:   $filter('i18n')('proxy.management.placeholder.first_name'),
            last_name:    $filter('i18n')('proxy.management.placeholder.last_name'),
            email:        $filter('i18n')('proxy.management.placeholder.email'),
            verify_email: $filter('i18n')('proxy.management.placeholder.verifyEmail'),
            relationship: $filter('i18n')('proxy.management.placeholder.relationship'),
            desc:         $filter('i18n')('proxy.management.label.description'),
            passphrase:   $filter('i18n')('proxy.management.label.passphrase')
        };
        $scope.relationshipChoices = [
            // TODO: BELOW CHOICES ARE PLACEHOLDERS. IMPLEMENT DYNAMICALLY WITH I18N FILTER, ALONG THE LINES OF EXAMPLE BELOW:
            // {code: 'P', description: $filter('i18n')('proxy.personalinformation.label.parent')},
            {code: 'P', description: 'Parent'},
            {code: 'G', description: 'Guardian'},
            {code: 'A', description: 'Advisor'}
        ];


        // INITIALIZE
        // ----------
        init();

    }
]);
