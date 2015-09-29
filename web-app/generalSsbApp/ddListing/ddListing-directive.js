/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
generalSsbAppDirectives.directive('accountInfo',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/accountInformation.html',
        scope: {
            acct: '='
        }
    };
}]);

generalSsbAppDirectives.directive('accountInfoDesktop',[function () {
    return{
        restrict: 'A',
        templateUrl: '../generalSsbApp/ddListing/accountInformationDesktop.html',
        scope: {
            acct: '='
        }
    };
}]);

generalSsbAppDirectives.directive('accountType',[function () {
    return{
        restrict: 'E',
        template: "{{(acct.accountType === 'C' ? 'directDeposit.account.type.checking' : 'directDeposit.account.type.savings')|i18n}}",
        scope: {
            acct: '='
        }
    };
}]);

generalSsbAppDirectives.directive('accountStatus',[function () {
    return{
        restrict: 'E',
        template: "{{(acct.status === 'P' ? 'directDeposit.account.status.prenote' : 'directDeposit.account.status.active')|i18n}}",
        scope: {
            acct: '='
        }
    };
}]);

generalSsbAppDirectives.directive('listingPanelPopulated',[function () {
    return{
        restrict: 'E',
        templateUrl: function() {
            var type = isDesktop() ? 'Desktop' : '';
            return '../generalSsbApp/ddListing/listingPanelPopulated' + type + '.html'
        },
        scope: {
            accounts: '=',
            apListingColumns: '='
        }
    };
}]);

generalSsbAppDirectives.directive('listingPanelNonpopulated',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/listingPanelNonpopulated.html',
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
