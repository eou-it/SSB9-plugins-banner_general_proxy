/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyManagementApp.filter('webAppResourcePath', ['webAppResourcePathString', function (webAppResourcePathString) {
    return function(input){
        var separator = input[0] === '/' ? '' : '/';
        return webAppResourcePathString + separator + input;
    };
}]);