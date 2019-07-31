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

proxyMgmtAppDirectives.directive('proxyMgmtMobileFooterButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '='
        },
        transclude: true,
        templateUrl: $filter('webAppResourcePath')('proxyManagementApp/proxyManagementHome/proxyMgmtMobileFooterButton.html')
    };
}]);

proxyMgmtAppDirectives.directive('proxyMgmtPopoverTooltip', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            popoverContents: '@'
        },
        templateUrl: $filter('webAppResourcePath')('proxyManagementApp/proxyManagementHome/proxyMgmtPopoverTooltip.html')
    };
}]);



