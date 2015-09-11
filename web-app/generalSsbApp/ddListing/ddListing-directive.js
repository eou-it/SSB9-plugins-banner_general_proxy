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

generalSsbAppDirectives.directive('replaceAccountPanel',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/replaceAccountPanel.html',
        scope: {
            accounts: '='
        }
    };
}]);

generalSsbAppDirectives.directive('addAccountPanel',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/addAccountPanel.html',
    };
}]);
