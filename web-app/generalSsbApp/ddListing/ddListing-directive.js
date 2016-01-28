/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

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

generalSsbAppDirectives.directive('accountStatus', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/accountStatus.html',
        scope: {
            account: '=',
            type: '@'
        },
        link: function(scope, element, attrs) {
            // Observe "account" to be sure it has been (re)loaded when a change is made, e.g. when a new account created
            attrs.$observe('account', function() {
                var isPrenote = scope.account.status === 'P',
                    statusProp = isPrenote ? 'directDeposit.account.status.prenote' : 'directDeposit.account.status.active';

                scope.statusText = $filter('i18n')(statusProp);
                scope.statusClass = isPrenote ? 'status-prenote' : 'status-active';
            });
        }
    };
}]);

generalSsbAppDirectives.directive('payListingPanelPopulatedMostRecent',[function () {
    return{
        restrict: 'E',
        link: function(scope) {
            var type = scope.isDesktopView ? 'Desktop' : '';
            scope.mostRecentPayPanelPopulatedTemplate = '../generalSsbApp/ddListing/payListingPanelPopulatedMostRecent' + type + '.html'
        },
        template: '<div ng-include="mostRecentPayPanelPopulatedTemplate"></div>'
    };
}]);

generalSsbAppDirectives.directive('payAccountInfoMostRecent',[function () {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationMostRecent.html',
        scope: {
            payHistoryDist: '='
        }
    };
}]);

/* 
 * relies on the dist variable from the ng-repeat in payListingPanelPopulatedMostRecentDesktop.html 
 */
generalSsbAppDirectives.directive('payAccountInfoMostRecentDesktop',[function () {
    return{
        restrict: 'A',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationMostRecentDesktop.html'
    };
}]);

generalSsbAppDirectives.directive('payListingPanelPopulatedProposed',[function () {
    return{
        restrict: 'E',
        link: function(scope) {
            var type = scope.isDesktopView ? 'Desktop' : '';
            scope.proposedPayPanelPopulatedTemplate = '../generalSsbApp/ddListing/payListingPanelPopulatedProposed' + type + '.html'
        },
        template: '<div ng-include="proposedPayPanelPopulatedTemplate"></div>'
    };
}]);

generalSsbAppDirectives.directive('payAccountInfoProposed', ['ddEditAccountService', function (ddEditAccountService) {
    return{
        restrict: 'E',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationProposed.html',
        controller: 'ddListingController',
        link: function(scope, elem, attrs, ctrl){
            scope.alloc = scope.allocation;
            
            scope.alloc.amountType = ddEditAccountService.getAmountType(scope.alloc);

            scope.showEditPayroll = function(){
                scope.showEditAccount(scope.alloc, 'HR');
            };
        }
    };
}]);

/* 
 * relies on the allocation variable from the ng-repeat in payListingPanelPopulatedProposedDesktop.html 
 */
generalSsbAppDirectives.directive('payAccountInfoProposedDesktop',['ddEditAccountService', '$filter', function (ddEditAccountService, $filter) {
    return{
        restrict: 'A',
        templateUrl: '../generalSsbApp/ddListing/payAccountInformationProposedDesktop.html',
        controller: 'ddListingController',
        link: function(scope, elem, attrs, ctrl){
            scope.alloc = scope.allocation;
            
            scope.alloc.amountType = ddEditAccountService.getAmountType(scope.alloc);

            scope.setAllocationAcctType = function(type){
                scope.alloc.accountType = type;
            };

            scope.displayAllocationVal = function () {
                if(scope.alloc.amountType === 'remaining'){
                    scope.alloc.allocation = $filter('i18n')('directDeposit.account.label.remaining');
                }
                else if(scope.alloc.amountType === 'percentage'){
                    scope.alloc.allocation = (scope.alloc.percent ? scope.alloc.percent : '0') + '%';
                }
                else if(scope.alloc.amountType === 'amount'){
                    scope.alloc.allocation = $filter('currency')((scope.alloc.amount ? scope.alloc.amount : '0'));
                }
                return scope.alloc.allocation;
            };

            scope.priorities = ddEditAccountService.priorities;

            scope.setAccountPriority = function (priority) {
                if(scope.alloc.priority != priority) {
                    if(scope.alloc.percent !== 100) {
                        ddEditAccountService.doReorder = 'all';
                        ddEditAccountService.setAccountPriority(scope.alloc, priority);
                    }
                    else{
                        // TODO: can't reprioritize remaining
                    }
                }
            };

            // When the amount is "Remaining" for a given allocation, the business rule is that
            // that allocation's priority needs to be set to move the allocation to the end of the
            // list of allocations.
            scope.updatePriorityForAmount = function(alloc) {
                if (alloc.amountType === 'remaining') {
                    ddEditAccountService.doReorder = 'all';
                    ddEditAccountService.setAccountPriority(alloc, scope.priorities.length);
                    scope.$apply();
                }
            };
        }
    };
}]);

generalSsbAppDirectives.directive('stopClick', [function () {
    return {
        restrict: 'A',
        link: function (scope, elem, attrs) {

            elem.on('click', function(event) {
                event.stopPropagation();
            });
        }
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
            var type = scope.isDesktopView ? 'Desktop' : '';
            scope.apListingPanelPopulatedTemplate = '../generalSsbApp/ddListing/apListingPanelPopulated' + type + '.html'

            scope.showEditAP = function(){
                scope.showEditAccount(scope.apAccount, 'AP');
            };
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
