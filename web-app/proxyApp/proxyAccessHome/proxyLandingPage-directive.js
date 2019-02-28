/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('landingPageAppTile', [ '$state', '$rootScope', 'webAppResourcePathString', function ($state, $rootScope, webAppResourcePathString) {
    return{
        restrict: 'E',
        templateUrl: webAppResourcePathString + '/proxyApp/proxyAccessHome/proxyUserTile.html',
        scope: {
            proxyData: '='
        },
        link: function(scope){
            scope.isRTL = $rootScope.isRTL;
            scope.goProxyApp = function(url) {
                $state.go(url);
            };
        }

    };
}]);

proxyAppDirectives.directive('landingPageProxyTile', ['$state', '$rootScope','webAppResourcePathString', '$filter',
    function ($state,$rootScope, webAppResourcePathString, $filter) {
    return{
        restrict: 'E',
        templateUrl: webAppResourcePathString + '/proxyApp/proxyAccessHome/proxyStudentTile.html',
        scope: {
            proxyData: '='
        },
        link: function(scope) {
            scope.onTileSelect = function(pageUrl) {
                var proxyData = scope.proxyData;
                var setId = function() {
                        jQuery.ajax({
                            url: "proxy/setId",
                            data: {"id": proxyData.id},
                            async: false
                        });
                    },
                    goProxyApp = function(url) {

                        //clear storage for Term Selector
                        if (sessionStorage.getItem("termCode")){
                            sessionStorage.removeItem("termCode");
                        }

                        if (sessionStorage.getItem("termDesc")){
                            sessionStorage.removeItem("termDesc");
                        }

                        //clear storage for AidYear Selector
                        if (sessionStorage.getItem("aidYearCode")){
                            sessionStorage.removeItem("aidYearCode");
                        }

                        if (sessionStorage.getItem("aidYearDesc")){
                            sessionStorage.removeItem("aidYearDesc");
                        }


                        sessionStorage.setItem("id", proxyData.id);
                        sessionStorage.setItem("name", proxyData.desc);

                        $rootScope.studentName = proxyData.desc;
                        $state.go(url, {id: proxyData.id});
                    };

                setId();
                goProxyApp(pageUrl);
            };
            scope.setupSelectCtrlFocusser = function($selectCtrl) {
                $selectCtrl.focusserTitle = scope.proxyData.desc + '. '+ $filter('i18n')('proxy.landingPage.label.select.to.view');
            };
        }
    };
}]);
