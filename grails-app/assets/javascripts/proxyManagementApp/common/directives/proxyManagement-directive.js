/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyMgmtAppDirectives.directive('proxyMgmtDeleteButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '=',
            item: '=',
            buttonTitle: '@',
            enabled: '='
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

proxyMgmtAppDirectives.directive('piInputWatcher', [function () {
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



