/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppDirectives.directive('payAccountInfoMostRecent',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationMostRecent.html',
        scope: {
            payHistoryDist: '='
        }
    };
}]);

generalSsbAppDirectives.directive('payAccountInfoProposed', ['ddListingService', function (ddListingService) {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationProposed.html',
        link: function (scope, element, attrs) {
            // TODO: preliminary implementation for these values.  May need to refactor, e.g. in order to pass
            // in account and other data.
            scope.userAllocationAmount = ddListingService.getUserAllocationAmount();
            scope.distributionAmount = ddListingService.getDistributionAmount();
        },
        scope: {
            account: '='
        }
    };
}]);

generalSsbAppDirectives.directive('apAccountInfo',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/apAccountInformation.html'
    };
}]);

generalSsbAppDirectives.directive('apAccountInfoDesktop',[function () {
    return{
        restrict: 'A',
        templateUrl: '../generalSsbApp/ddListing/apAccountInformationDesktop.html'
    };
}]);

generalSsbAppDirectives.directive('accountType',[function () {
    return{
        restrict: 'E',
        template: "{{(account.accountType === 'C' ? 'directDeposit.account.type.checking' : 'directDeposit.account.type.savings')|i18n}}",
        scope: {
            account: '='
        }
    };
}]);

generalSsbAppDirectives.directive('accountStatus',[function () {
    return{
        restrict: 'E',
        template: "{{(account.status === 'P' ? 'directDeposit.account.status.prenote' : 'directDeposit.account.status.active')|i18n}}",
        scope: {
            account: '='
        }
    };
}]);

generalSsbAppDirectives.directive('payListingPanelPopulatedMostRecent',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payListingPanelPopulatedMostRecent.html'
    };
}]);

generalSsbAppDirectives.directive('payListingPanelPopulatedProposed',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payListingPanelPopulatedProposed.html'
    };
}]);

generalSsbAppDirectives.directive('payListingPanelNonpopulatedProposed',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payListingPanelNonpopulatedProposed.html'
    };
}]);

generalSsbAppDirectives.directive('apListingPanelPopulated',['ddEditAccountService', function (ddEditAccountService) {
    return{
        restrict: 'E',
        link: function(scope) {
            scope.editAccountService = ddEditAccountService; // TODO: is this still used?

            var type = scope.isDesktop() ? 'Desktop' : '';
            scope.apListingPanelPopulatedTemplate = '../generalSsbApp/ddListing/apListingPanelPopulated' + type + '.html'
        },
        template: '<div ng-include="apListingPanelPopulatedTemplate"></div>'
    };
}]);

generalSsbAppDirectives.directive('apListingPanelNonpopulated',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/apListingPanelNonpopulated.html',
    };
}]);

generalSsbAppDirectives.directive('notificationBox',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/ddNotificationBox.html',
        scope: {
            notificationText: '@'
        }
    };
}]);
