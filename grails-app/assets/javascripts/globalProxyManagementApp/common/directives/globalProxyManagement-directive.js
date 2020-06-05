/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

globalProxyMgmtAppDirectives.directive('globalProxyMgmtDeleteButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '=',
            item: '=',
            buttonTitle: '@',
            enabled: '='
        },
        templateUrl: $filter('webAppResourcePath')('globalProxyManagementApp/proxyManagementHome/globalProxyMgmtDeleteButton.html')
    };
}]);

globalProxyMgmtAppDirectives.directive('globalProxyMgmtMobileFooterButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '='
        },
        transclude: true,
        templateUrl: $filter('webAppResourcePath')('globalProxyManagementApp/proxyManagementHome/globalProxyMgmtMobileFooterButton.html')
    };
}]);

globalProxyMgmtAppDirectives.directive('globalProxyMgmtPopoverTooltip', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            popoverContents: '@'
        },
        templateUrl: $filter('webAppResourcePath')('globalProxyManagementApp/proxyManagementHome/globalProxyMgmtPopoverTooltip.html')
    };
}]);

globalProxyMgmtAppDirectives.directive('piInputWatcher', [function () {
    return {
        restrict: 'A',
        require: '?ngModel',
        link: function (scope, elem, attrs, ngModel) {

            // do nothing if ng-model is not present
            if (!ngModel) return;

            scope.$watch(
                // watch the value of the input
                function() {
                    return elem.val();
                },
                // change listener, update ngModel whenever watched value changes
                function(newValue, oldValue) {
                    if (newValue !== oldValue) {
                        ngModel.$setViewValue(newValue);
                    }
                }
            );
        }
    };
}]);



