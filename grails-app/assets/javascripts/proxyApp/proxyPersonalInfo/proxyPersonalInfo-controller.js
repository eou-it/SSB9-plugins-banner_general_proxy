/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope','$rootScope','$state','$filter','$location',
    'proxyAppService','notificationCenterService', 'proxyEmailService', 'proxyAppDateService', 'proxyAppBirthDateService',
    function ($scope, $rootScope, $state, $filter, $location, proxyAppService, notificationCenterService, proxyEmailService, proxyAppDateService, proxyAppBirthDateService) {

        var init = function () {
            getPersonalInfo();
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
        $scope.birthDateFocused;
        $scope.profile = {};
        $scope.emailErrMsg = '';
        $scope.birthDateErrMsg = '';
        $scope.personalInfoSections = [];
        $scope.legalSexChoices = [
            {code: 'M', description: $filter('i18n')('proxy.personalinformation.label.male')},
            {code: 'F', description: $filter('i18n')('proxy.personalinformation.label.female')},
            {code: 'N', description: $filter('i18n')('proxy.personalinformation.label.unknown')}
        ];

        $scope.setupSelectCtrlFocusser = function($selectCtrl, text) {
            $selectCtrl.focusserTitle = text;
        };

        $scope.setBirthDate = function(data){
            $scope.$apply(function () {
                var errors = $scope.setDataValidationErrors(data);
                if (!errors) {
                    $scope.profileElements["p_birth_date"].model = data;
                }
            })
        };

        var getPersonalInfo = function() {
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

                   if (elem.visible) {
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
                   }

                });


                if ($scope.detailsElements.length> 0){
                    $scope.personalInfoSections.push({heading: $filter('i18n')('proxy.personalinformation.label.personalDetails'), elements: $scope.detailsElements})
                }

                if ($scope.contactElements.length> 0){
                    $scope.personalInfoSections.push({heading: $filter('i18n')('proxy.personalinformation.label.contact'), elements: $scope.contactElements})
                }

                if ($scope.addressElements.length> 0){
                    $scope.personalInfoSections.push({heading: $filter('i18n')('proxy.personalinformation.label.address'), elements: $scope.addressElements})
                }

                if ($scope.otherElements.length> 0){
                    $scope.personalInfoSections.push({heading: $filter('i18n')('proxy.personalinformation.label.otherInfo'), elements: $scope.otherElements})
                }

                $scope.profileElements['p_birth_date'].isWidget = true;

                $scope.profileElements['p_cnty_code'].fetch = proxyAppService.getCountyList;
                $scope.profileElements['p_cnty_code'].isWidget = true;
                $scope.profileElements['p_cnty_code'].isDropdown = true;

                $scope.profileElements['p_stat_code'].fetch = proxyAppService.getStateList;
                $scope.profileElements['p_stat_code'].isWidget = true;
                $scope.profileElements['p_stat_code'].isDropdown = true;

                $scope.profileElements['p_natn_code'].fetch = proxyAppService.getNationList;
                $scope.profileElements['p_natn_code'].isWidget = true;
                $scope.profileElements['p_natn_code'].isDropdown = true;
                if($scope.proxyProfile['p_natn_code']) {
                    $scope.profileElements['p_natn_code'].model = {
                        code: $scope.proxyProfile['p_natn_code'].code,
                        description: $scope.proxyProfile['p_natn_code'].nation
                    };
                }

                $scope.profileElements['p_sex'].isWidget = true;
                if($scope.proxyProfile['p_sex']) {
                    $scope.profileElements['p_sex'].model = {
                        code: $scope.profileElements['p_sex'].model,
                        description: $scope.profileElements['p_sex'].model === 'M' ? $filter('i18n')('proxy.personalinformation.label.male') :
                            $scope.profileElements['p_sex'].model === 'F' ? $filter('i18n')('proxy.personalinformation.label.female') :
                                $filter('i18n')('proxy.personalinformation.label.unknown')
                    };
                }


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
        },
            displayErrors = function(errors) {
                var numberOfErrors = errors.length;
                notificationCenterService.clearNotifications();
                for (var i = 0; i < numberOfErrors; i++) {
                    notificationCenterService.addNotification(errors[i], "error", true);
                }
            };
        $scope.setDataValidationErrors = function (birthDate) {
            var emailErrorMessage,
                birthDateErrorMessage,
                errors = [];

            emailErrorMessage = proxyEmailService.getErrorEmailAddress($scope.profile.p_email_address);

            if (emailErrorMessage) {
                $scope.emailErrMsg = emailErrorMessage;
                errors.push(emailErrorMessage);
            } else {
                $scope.emailErrMsg = '';
            }
            birthDateErrorMessage = proxyAppBirthDateService.getErrorBirthDate(birthDate);
            if (birthDateErrorMessage) {
                $scope.birthDateErrMsg = birthDateErrorMessage;
                errors.push(birthDateErrorMessage);

            } else {
                $scope.birthDateErrMsg = '';
                if ($scope.profileElements["p_birth_date"].model) {
                    $scope.proxyProfile.p_birth_date = proxyAppDateService.stringToDate($scope.profileElements["p_birth_date"].model);
                    $scope.profile.p_birth_date = $scope.proxyProfile.p_birth_date;
                }
            }
            displayErrors(errors);
            return errors;

        };

        $scope.save = function() {
            $scope.profile = {};

            _.each(Object.keys($scope.profileElements), function(it) {
                $scope.profile[it] = $scope.profileElements[it].model;
            });

            var errors = $scope.setDataValidationErrors($scope.profile.p_birth_date);
            if (errors.length !== 0) {
                return; //Do not update.
            }

            if ($scope.profile.p_sex) {
                $scope.profile.p_sex = $scope.profile.p_sex.code;
            }

            //Show a Wait Message. It will be replaced by Success message after response is processed.
            notificationCenterService.addNotification('proxy.personalinformation.onSave.waitMessage', 'success', true);

            proxyAppService.updateProxyPersonalInfo($scope.profile).$promise.then(function(response) {
                var notifications = [],
                    doStateGoSuccess = function(messageOnSave) {
                        notifications.push({message:  messageOnSave ? messageOnSave : 'proxy.personalinformation.label.saveSuccess',
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

                    doStateGoSuccess(response.message);
                }

            });
        };

        $scope.setBirthDateFocused = function (focused) {
            $scope.birthDateFocused = focused;
        };

        $scope.$watch('profileElements["p_birth_date"].model', function (newVal, oldVal) {
            if (newVal !== oldVal && !$scope.birthDateFocused) {
                    $scope.setDataValidationErrors(newVal)
            }
        });

        $scope.cancel = function() {
            // If proxy personal info has never been filled out successfully, Cancel just returns the form to its
            // original state, wiping out any unsaved user changes.
            // If it *has* previously been filled out successfully -- meaning this is just an update -- Cancel
            // simply returns to the landing page.
            if ($rootScope.profileRequired) {
                getPersonalInfo();
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
