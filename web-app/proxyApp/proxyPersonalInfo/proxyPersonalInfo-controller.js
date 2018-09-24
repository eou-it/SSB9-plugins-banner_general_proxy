/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
proxyAppControllers.controller('proxyPersonalInformationController',['$scope','$rootScope','proxyAppService','$filter',
    function ($scope, $rootScope, proxyAppService, $filter) {

        $scope.proxyProfile = {};
        $scope.proxyUiRules = {};
        $scope.profileElements = {};


        proxyAppService.getProxyPersonalInfo().$promise.then(function(response) {
            $scope.proxyProfile = response.proxyProfile;
            $scope.proxyUiRules = response.proxyUiRules;

            _.each(Object.keys($scope.proxyProfile), function(it){
                var required = $scope.proxyUiRules[it] ? $scope.proxyUiRules[it].required : false;

                $scope.profileElements[it] = {
                    label: required ? ($filter('i18n')('proxy.personalinformation.label.'+it)+ '<font color="red">*</font>' ) : $filter('i18n')('proxy.personalinformation.label.'+it),
                    model: $scope.proxyProfile[it],
                    fieldLength: $scope.proxyUiRules[it].fieldLength,
                    elemId: it,
                    visible: $scope.proxyUiRules[it].visible === undefined ? true : $scope.proxyUiRules[it].visible,
                    isWidget: false,
                    isDropdown: false
                };

            });


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

        $scope.save = function() {
            var profile = {};


            _.each(Object.keys($scope.profileElements), function(it) {
                profile[it] = $scope.profileElements[it].model
            });

            console.dir(profile);

            proxyAppService.updateProxyPersonalInfo(profile).$promise.then(function(response) {
                if(response.failure) {
                    $scope.flashMessage = response.message;

                    if ($('#breadcrumb-panel').is(":visible"))
                        $("#breadcrumb-panel").hide();
                }
                else {
                    $scope.flashMessage = 'Saved successfully';
                    $rootScope.profileRequired = false;
                    if (!$('#breadcrumb-panel').is(":visible"))
                        $("#breadcrumb-panel").show();
                }

            });
        };

    }
]);
