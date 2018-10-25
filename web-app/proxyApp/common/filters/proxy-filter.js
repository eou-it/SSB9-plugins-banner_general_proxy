/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyApp.filter('emptyValToDash', [function () {
    return function(input){
        return (input ? input : '-');
    };
}]);
