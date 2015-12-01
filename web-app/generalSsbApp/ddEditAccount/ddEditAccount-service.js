/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddEditAccountService', ['$resource', function ($resource) {
    var createAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'createAccount'}, {save: {method:'POST'}}),

        updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'updateAccount'}, {save: {method:'POST'}}),

        deleteAccounts = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'deleteAccounts'}, {delete: {method:'POST', isArray:true}}),

        bankInfo = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getBankInfo'}, {query: {method:'GET', isArray:false}});

    this.saveAccount = function (account, createNew) {
        return createNew ? createAccount.save(account) : updateAccount.save(account);
    };

    this.deleteAccounts = function (accounts) {
        return deleteAccounts.delete(accounts);
    };

    this.getBankInfo = function (routingNum) {
        return bankInfo.query({bankRoutingNum: routingNum});
    };
    
    var validAccount = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'validateAccountNum'}, {query: {method:'GET', isArray:false}});
    
    this.validateAccountNum = function (accountNum) {
        return validAccount.query({bankAccountNum: accountNum});
    };

    this.setAccountType = function (acct, acctType) {
        acct.accountType = acctType;
    };

}]);