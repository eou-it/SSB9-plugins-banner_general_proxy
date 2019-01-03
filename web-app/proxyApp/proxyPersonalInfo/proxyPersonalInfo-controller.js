/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope','$rootScope','$state','$filter','$location',
    'proxyAppService','notificationCenterService','proxyEmailService',
    function ($scope, $rootScope, $state, $filter, $location, proxyAppService, notificationCenterService, proxyEmailService) {

        var init = function () {
            $scope.getPersonalInfo();
        };

        $scope.guestUserName = CommonContext.user;

        $scope.proxyProfile;
        $scope.proxyUiRules;

        $scope.profileElements;
        $scope.detailsElements;
        $scope.contactElements;
        $scope.addressElements;
        $scope.otherElements;
        $scope.optOutAdvDate;
        $scope.personalInfoSections = [];

        $scope.setBirthDate = function(data){
            $scope.proxyProfile.p_birth_date = data;
        };

        $scope.getPersonalInfo = function() {
            $scope.profileElements = {};
            $scope.detailsElements = [];
            $scope.contactElements = [];
            $scope.addressElements = [];
            $scope.otherElements   = [];
            $scope.optOutAdvDate = null;

            proxyAppService.getProxyPersonalInfo().$promise.then(function(response) {
                var detailsIds = ['p_name_prefix', 'p_first_name', 'p_mi', 'p_surname_prefix', 'p_last_name', 'p_name_suffix', 'p_pref_first_name'],
                    contactIds = ['p_email_address', 'p_ctry_code_phone', 'p_phone_area', 'p_phone_number', 'p_phone_ext'],
                    addressIds = ['p_house_number', 'p_street_line1', 'p_street_line2', 'p_street_line3', 'p_street_line4', 'p_city', 'p_stat_code', 'p_zip', 'p_natn_code', 'p_cnty_code', ''],
                    elem;

                $scope.proxyProfile = response.proxyProfile;
                $scope.proxyUiRules = response.proxyUiRules;

                _.each(Object.keys($scope.proxyProfile), function (it) {
                    var required = $scope.proxyUiRules[it] ? $scope.proxyUiRules[it].required : false;

                    $scope.profileElements[it] = {
                        label: required ? ($filter('i18n')('proxy.personalinformation.label.' + it) + '<font color="#CD3B3E">*</font>') : $filter('i18n')('proxy.personalinformation.label.' + it),
                        model: $scope.proxyProfile[it],
                        fieldLength: $scope.proxyUiRules[it].fieldLength,
                        elemId: it,
                        visible: $scope.proxyUiRules[it].visible === undefined ? true : $scope.proxyUiRules[it].visible,
                        isWidget: false,
                        isDropdown: false
                    };

                    // Group elements for presentation on page
                    elem = $scope.profileElements[it];

                    if ($.inArray(it, detailsIds) > -1) {
                        $scope.detailsElements.push(elem);
                    } else if ($.inArray(it, contactIds) > -1) {
                        $scope.contactElements.push(elem);
                    } else if ($.inArray(it, addressIds) > -1) {
                        $scope.addressElements.push(elem);
                    } else if (it === 'p_opt_out_adv_date') {
                        $scope.optOutAdvDate = elem;
                    } else {
                        $scope.otherElements.push(elem);
                    }

                });

                $scope.personalInfoSections = [
                    {heading: 'Personal Details', elements: $scope.detailsElements},
                    {heading: 'Contact',          elements: $scope.contactElements},
                    {heading: 'Address',          elements: $scope.addressElements},
                    {heading: 'Other Info',       elements: $scope.otherElements}
                ];

                $scope.profileElements['p_birth_date'].isWidget = true;

                $scope.profileElements['p_cnty_code'].fetch = 'County';
                $scope.profileElements['p_cnty_code'].isWidget = true;
                $scope.profileElements['p_cnty_code'].isDropdown = true;

                $scope.profileElements['p_stat_code'].fetch = 'State';
                $scope.profileElements['p_stat_code'].isWidget = true;
                $scope.profileElements['p_stat_code'].isDropdown = true;

                $scope.profileElements['p_natn_code'].fetch = 'Nation';
                $scope.profileElements['p_natn_code'].isWidget = true;
                $scope.profileElements['p_natn_code'].isDropdown = true;

                $scope.profileElements['p_sex'].isWidget = true;

                $scope.profileElements['p_opt_out_adv_date'].isWidget = true;

                // assign placeholders

                $scope.profileElements['p_name_prefix'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_name_prefix');
                $scope.profileElements['p_first_name'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_first_name');
                $scope.profileElements['p_mi'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_mi');
                $scope.profileElements['p_surname_prefix'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_surname_prefix');
                $scope.profileElements['p_last_name'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_last_name');
                $scope.profileElements['p_name_suffix'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_name_suffix');
                $scope.profileElements['p_pref_first_name'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_pref_first_name');
                $scope.profileElements['p_email_address'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_email_address');
                $scope.profileElements['p_ctry_code_phone'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_ctry_code_phone');
                $scope.profileElements['p_phone_area'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_phone_area');
                $scope.profileElements['p_phone_number'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_phone_number');
                $scope.profileElements['p_phone_ext'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_phone_ext');
                $scope.profileElements['p_house_number'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_house_number');
                $scope.profileElements['p_street_line1'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_street_line1');
                $scope.profileElements['p_street_line2'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_street_line2');
                $scope.profileElements['p_street_line3'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_street_line3');
                $scope.profileElements['p_street_line4'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_street_line4');
                $scope.profileElements['p_street_line4'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_street_line4');
                $scope.profileElements['p_city'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_city');
                $scope.profileElements['p_stat_code'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_stat_code');
                $scope.profileElements['p_zip'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_zip');
                $scope.profileElements['p_natn_code'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_natn_code');
                $scope.profileElements['p_cnty_code'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_cnty_code');
                $scope.profileElements['p_sex'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_sex');
                $scope.profileElements['p_birth_date'].placeholder = $filter('i18n')('proxy.personalinformation.label.p_birth_date');
                $scope.profileElements['p_ssn'].placeholder = $filter('i18n')('proxy.personalinformation.p_ssn');

            });
        };

        $scope.save = function() {
            var profile = {},
                errorMsg;

            _.each(Object.keys($scope.profileElements), function(it) {
                profile[it] = $scope.profileElements[it].model;
            });

            if ($scope.profileElements["p_birth_date"].model) {
                $scope.proxyProfile.p_birth_date = proxyAppService.stringToDate($scope.profileElements["p_birth_date"].model);

                profile.p_birth_date = $scope.proxyProfile.p_birth_date;
            }

            if (profile.p_email_address) {
                errorMsg = proxyEmailService.getErrorEmailAddress(profile.p_email_address);

                if (errorMsg) {
                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification(errorMsg, "error", true);

                    return; // DO NOT UPDATE
                }
            }

            proxyAppService.updateProxyPersonalInfo(profile).$promise.then(function(response) {
                var notifications = [],
                    doStateGoSuccess = function() {
                        notifications.push({message: 'proxy.personalinformation.label.saveSuccess',
                            messageType: $scope.notificationSuccessType,
                            flashType: $scope.flashNotification});

                        $state.go('home',
                            {onLoadNotifications: notifications},
                            {reload: true, inherit: false, notify: true}
                        );
                    };

                if(response.failure) {
                    $scope.flashMessage = response.message;

                    notificationCenterService.clearNotifications();
                    notificationCenterService.addNotification(response.message, "error", true);

                    if ($rootScope.profileRequired && $('#breadcrumb-panel').is(":visible")) {
                        $("#breadcrumb-panel").hide();
                    }
                }
                else {
                    $rootScope.profileRequired = false;

                    if (!$('#breadcrumb-panel').is(":visible"))
                        $("#breadcrumb-panel").show();

                    doStateGoSuccess();
                }

            });
        };

        $scope.cancel = function() {
            // If proxy personal info has never been filled out successfully, Cancel just returns the form to its
            // original state, wiping out any unsaved user changes.
            // If it *has* previously been filled out successfully -- meaning this is just an update -- Cancel
            // simply returns to the landing page.
            if ($rootScope.profileRequired) {
                $scope.getPersonalInfo();
            } else {
                $state.go('home',
                    {reload: true, inherit: false, notify: true}
                );
            }
        };


        // INITIALIZE
        // ----------
        init();

    }
]);
