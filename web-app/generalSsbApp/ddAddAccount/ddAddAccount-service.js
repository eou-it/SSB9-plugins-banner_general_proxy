/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('ddAddAccountService', ['$resource', function ($resource) {
    var updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'createAccount'}, {save: {method:'POST'}});

    this.createApAccount = function (account) {
        return updateAccount.save(account);
    };
}]);
