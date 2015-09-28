/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddAddAccountService', ['$resource', function ($resource) {
    var updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'createAccount'}, {save: {method:'POST'}});

    this.createApAccount = function (account) {
        return updateAccount.save(account);
    };
    
    var bankInfo = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getBankInfo'}, {query: {method:'GET', isArray:false}});
    
    this.getBankInfo = function (routingNum) {
        return bankInfo.query({bankRoutingNum: routingNum});
    };
    
    var validAccount = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'validateAccountNum'}, {query: {method:'GET', isArray:false}});
    
    this.validateAccountNum = function (accountNum) {
        return validAccount.query({bankAccountNum: accountNum});
    };
}]);
