/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyMgmtAppDirectives.directive('proxyMgmtDeleteButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '=',
            item: '=',
            buttonTitle: '@'
        },
        templateUrl: $filter('webAppResourcePath')('proxyManagementApp/proxyManagementHome/proxyMgmtDeleteButton.html')
    };
}]);
