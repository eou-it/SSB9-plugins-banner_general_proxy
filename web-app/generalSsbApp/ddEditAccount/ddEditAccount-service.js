/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositEditAccountService', ['$resource', function ($resource) {

    var doUpdate = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'updateAccount'}, {save: {method:'POST'}});

    this.updateAccount = function (account) {
        return doUpdate.save(account);
    };

}]);
