/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddEditAccountService', ['directDepositService', '$resource',
    function (directDepositService, $resource) {

    var createAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'createAccount'}, {save: {method:'POST'}}),

        updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'updateAccount'}, {save: {method:'POST'}}),

        reorderAllAccounts = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'reorderAllAccounts'}, {save: {method:'POST', isArray:true}}),
        
        reorderAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'reorderAccounts'}, {save: {method:'POST', isArray:true}}),

        deleteAccounts = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'deleteAccounts'}, {delete: {method:'POST', isArray:true}}),

        bankInfo = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getBankInfo'}, {query: {method:'GET', isArray:false}});

    this.saveAccount = function (account, createNew) {
        if(createNew){
            if(this.doReorder === 'new') {
                account.newPosition = account.priority;
            }
            return createAccount.save(account);
        }
        else {
            // set the priority back to one received from database if updating payroll account
            if(account.hrIndicator === 'A'){
                account.priority = this.priorities[account.priority-1].persistVal;
            }
            return updateAccount.save(account);
        }
    };
    
    this.reorderAccounts = function (account) {
        if(this.doReorder === 'all') {
            var i;
            for(i = 0; i < this.accounts.length; i++) {
                this.accounts[i].priority = this.priorities[i].persistVal;
            }
            return reorderAllAccounts.save(this.accounts);
        }
        else if(this.doReorder === 'single') {
            account.newPosition = account.priority;
            account.priority = this.priorities[account.priority-1].persistVal;

            return reorderAccount.save(account);
        }
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
        if(acct.allocation === '100%'){
            acct.percent = 100;
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
        if(directDepositService.isRemaining(acct)){
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
    this.accounts = [0];
    this.doReorder = false;
    
    this.setupPriorities = function ( accts ) {
        // create priority list and normalize account priorities
        var i;
        for(i = 0; i < accts.length; i++){
            var priorityInfo = {displayVal: i+1, persistVal: accts[i].priority};
            accts[i].priority = i+1;
            this.priorities[i] = priorityInfo;
        }
        this.accounts = accts;
    };
    
    this.setAccountPriority = function (acct, priority) {
        var i,
            numAcctsToSort = 0,
            lastAccount = this.accounts[this.accounts.length-1];
        
        if(directDepositService.isRemaining(lastAccount)) {
            // don't move the remaining
            numAcctsToSort = this.accounts.length-1;
        }
        else {
            numAcctsToSort = this.accounts.length;
        }
        
        if(acct.priority < priority){
            // decrease acct's priority i.e. will be allocated later
            for(i = 0; i < numAcctsToSort; i++) {
                if (this.accounts[i].priority <= priority) {
                    this.accounts[i].priority--;
                }
            }
        }
        else if(acct.priority > priority){
            // increase acct's priority i.e. will be allocated earlier
            for(i = 0; i < numAcctsToSort; i++) {
                if(this.accounts[i].priority >= priority){
                    this.accounts[i].priority++;
                }
            }
        }
        acct.priority = priority;
        this.accounts.sort(function(a, b){
            return a.priority - b.priority;
        });
        this.accounts = this.normalize(this.accounts);
    };
    
    this.normalize = function (accts) {
        var i;
        for(i = 0; i < accts.length; i++) {
            accts[i].priority = i+1;
        }
        return accts;
    };

}]);
