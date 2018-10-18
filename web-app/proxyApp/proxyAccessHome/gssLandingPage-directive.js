/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('landingPageAppTile', [ '$state', 'webAppResourcePathString', function ($state, webAppResourcePathString) {
    return{
        restrict: 'E',
        templateUrl: webAppResourcePathString + '/proxyApp/proxyAccessHome/gssAppTile.html',
        scope: {
            proxyData: '='
        },
        link: function(scope){
            scope.goProxyApp = function(url) {
                $state.go(url);
            };
        }

    };
}]);

proxyAppDirectives.directive('landingPageProxyTile', ['$state', '$rootScope','webAppResourcePathString', function ($state,$rootScope, webAppResourcePathString) {
    return{
        restrict: 'E',
        templateUrl: webAppResourcePathString + '/proxyApp/proxyAccessHome/gssProxyTile.html',
        scope: {
            proxyData: '='
        },
        link: function(scope){
            scope.goProxyApp = function(url) {

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


                sessionStorage.setItem("pidm", scope.proxyData.pidm);
                sessionStorage.setItem("name", scope.proxyData.desc);
                
                $rootScope.studentName = scope.proxyData.desc;
                $state.go(url, {pidm: scope.proxyData.pidm});
            };

            scope.setPidm = function() {
                jQuery.ajax({
                    url: "proxy/setPidm",
                    data: {"pidm": scope.proxyData.pidm},
                    async: false
                });

                _.each(scope.$parent.proxyTiles, function(value) {

                    if (value.pidm != scope.proxyData.pidm)
                        value.open = false;
                });

            };
        }

    };
}]);
