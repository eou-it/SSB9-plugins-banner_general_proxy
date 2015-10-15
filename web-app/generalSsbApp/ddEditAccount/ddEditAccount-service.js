/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddEditAccountService', ['$resource', function ($resource) {
    var createAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'createAccount'}, {save: {method:'POST'}}),

        updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'updateAccount'}, {save: {method:'POST'}});

    this.saveApAccount = function (account, createNew) {
        return createNew ? createAccount.save(account) : updateAccount.save(account);
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
