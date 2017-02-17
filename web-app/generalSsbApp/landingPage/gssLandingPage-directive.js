/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppDirectives.directive('landingPageAppTile', [function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/landingPage/gssAppTile.html',
        scope: {
            tileData: '='
        },
        link: function(scope){
            scope.goApp = function(url) {
                window.location.href = url;
            };
        }

    };
}]);
