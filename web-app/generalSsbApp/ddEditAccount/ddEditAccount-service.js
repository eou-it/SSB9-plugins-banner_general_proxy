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
    
    this.getAmountType = function (acct) {
        if(acct.allocation === 'Remaining'){
            acct.percent = null;
            acct.amount = null;
            return 'remaining';
        }
        else if(acct.amount != null){
            return 'amount';
        }
        else if(acct.percent != null){
            return 'percentage';
        }
    };
    
    this.setAmountValues = function (acct, amountType){
        if(amountType === 'remaining'){
            acct.percent = 100;
            acct.amount = ''; // grails will ignore null values, so use empty strings instead
        }
        else if(amountType === 'amount'){
            acct.percent = '';
        }
        else if(amountType === 'percentage'){
            acct.amount = '';
        }
    };

    this.syncedAccounts = false;

    this.setSyncedAccounts = function(val){this.syncedAccounts = val;};

    this.priorities = [0];
    
    this.setPriorities = function ( accts ) {
        // create priority list and normalize account priorities (might not be neccessary).
    	var i;
    	for(i = 0; i < accts.length; i++) {
            accts[i].priority = i+1;
            this.priorities[i] = i+1;
        }
    };

    this.setAccountPriority = function (acct, priority) {
        acct.priority = priority;
    };

}]);
