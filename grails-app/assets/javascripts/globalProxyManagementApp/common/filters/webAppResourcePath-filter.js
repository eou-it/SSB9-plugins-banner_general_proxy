/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.filter('webAppResourcePath', ['webAppResourcePathString', function (webAppResourcePathString) {
    return function(input){
        const separator = input[0] === '/' ? '' : '/';
        return webAppResourcePathString + separator + input;
    };
}]);
