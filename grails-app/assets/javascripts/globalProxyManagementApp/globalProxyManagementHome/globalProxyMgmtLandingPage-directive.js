/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.directive('globalLandingPageProxyTile', ['$state', '$rootScope', '$filter',
    function ($state, $rootScope, $filter) {
    return{
        restrict: 'E',
        templateUrl: '../assets/globalProxyManagementApp/globalProxyManagementHome/globalProxyMgmtStudentTile.html',
        scope: {
            proxyData: '=',
            deleteFunction: '&'
        },
        link: function(scope) {
            scope.onTileSelect = function(pageUrl) {
                const proxyData = scope.proxyData;
                const setId = function () {
                        jQuery.ajax({
                            url: "proxy/setId",
                            data: {"id": proxyData.id},
                            async: false
                        });
                    },
                    goProxyApp = function (url) {

                        //clear storage for Term Selector
                        if (sessionStorage.getItem("termCode")) {
                            sessionStorage.removeItem("termCode");
                        }

                        if (sessionStorage.getItem("termDesc")) {
                            sessionStorage.removeItem("termDesc");
                        }

                        //clear storage for AidYear Selector
                        if (sessionStorage.getItem("aidYearCode")) {
                            sessionStorage.removeItem("aidYearCode");
                        }

                        if (sessionStorage.getItem("aidYearDesc")) {
                            sessionStorage.removeItem("aidYearDesc");
                        }

                        sessionStorage.setItem("id", proxyData.id);
                        sessionStorage.setItem("name", proxyData.desc);

                        $rootScope.studentName = proxyData.desc;

                        if (url.indexOf("proxy") > -1) {
                            $state.go(url, {id: proxyData.id});
                        } else {
                            if (!!window.mepCode) {
                                window.open(
                                    encodeURI($rootScope.studenSSB + "/proxy/navigate?url=" + url + "&token=" + scope.proxyData.token + "&mepCode=" + window.mepCode), "_self"
                                )
                            } else {
                                window.open(
                                    encodeURI($rootScope.studenSSB + "/proxy/navigate?url=" + url + "&token=" + scope.proxyData.token), "_self"
                                )
                            }
                        }
                    };

                //setId();
                goProxyApp(pageUrl);
            };
            scope.setupSelectCtrlFocusser = function($selectCtrl) {
                $selectCtrl.focusserTitle = scope.proxyData.desc + '. '+ $filter('i18n')('proxy.landingPage.label.select.to.view');
            };
        }
    };
}]);
