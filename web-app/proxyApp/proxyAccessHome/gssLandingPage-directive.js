/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('landingPageAppTile', [ '$state', function ($state) {
    return{
        restrict: 'E',
        templateUrl: '../proxyApp/proxyAccessHome/gssAppTile.html',
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

proxyAppDirectives.directive('landingPageProxyTile', [function () {
    return{
        restrict: 'E',
        templateUrl: '../proxyApp/proxyAccessHome/gssProxyTile.html',
        scope: {
            proxyData: '='
        },
        link: function(scope){
            scope.goProxyApp = function(url) {
                window.location.href = $('meta[name=applicationContextRoot]').attr("content") + url;
            };
        }

    };
}]);