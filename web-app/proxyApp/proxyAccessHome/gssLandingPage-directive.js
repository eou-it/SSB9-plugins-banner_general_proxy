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
                $rootScope.studentName = scope.proxyData.desc;
                $state.go(url, {pidm: scope.proxyData.pidm});
            };

            scope.setPidm = function() {
                jQuery.ajax({
                    url: "proxy/setPidm",
                    data: {"pidm": scope.proxyData.pidm},
                    async: false
                });

            };
        }

    };
}]);
