/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope','$rootScope','$state','$filter','$location','proxyAppService','notificationCenterService',
    function ($scope, $rootScope, $state, $filter, $location, proxyAppService, notificationCenterService) {

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
            $scope.proxyProfile.p_birth_date = proxyAppService.stringToDate(data);
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

                // Date Conversion TODO
                if (response.proxyProfile.p_birth_date != null && response.proxyProfile.p_birth_date.length != 0) {
                    var locale = $('meta[name=locale]').attr("content");
                    $scope.proxyProfile.p_birth_date = (new Date(response.proxyProfile.p_birth_date)).toLocaleDateString(locale);
                }

                _.each(Object.keys($scope.proxyProfile), function (it) {
                    var required = $scope.proxyUiRules[it] ? $scope.proxyUiRules[it].required : false;

                    $scope.profileElements[it] = {
                        label: required ? ($filter('i18n')('proxy.personalinformation.label.' + it) + '<font color="red">*</font>') : $filter('i18n')('proxy.personalinformation.label.' + it),
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
            });
        };

        $scope.save = function() {
            var profile = {};

            _.each(Object.keys($scope.profileElements), function(it) {
                profile[it] = $scope.profileElements[it].model
            });


            // Date Conversion TODO
            if ($scope.profileElements["p_birth_date"].model.length != 0) {
                $scope.proxyProfile.p_birth_date = proxyAppService.stringToDate($scope.profileElements["p_birth_date"].model);

                var str = ($scope.proxyProfile.p_birth_date.getUTCMonth() + 1).toString() + "/" +
                    $scope.proxyProfile.p_birth_date.getUTCDate() +
                    "/" + $scope.proxyProfile.p_birth_date.getUTCFullYear().toString();

                profile.p_birth_date = str;
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
