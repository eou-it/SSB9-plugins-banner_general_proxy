/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositEditAccountService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

    var updateAccount = $resource('../ssb/:controller/:action',
        {controller: 'UpdateAccount', action: 'updateAccount'}, {save: {method:'POST'}});

    this.authorizedChanges = false;

    this.updateApAccount = function (account) {
        var self = this;

        updateAccount.save(account).$promise.then(function (response) {
            if(response.failure) {
                notificationCenterService.displayNotifications(response.message, "error");
            }
            else {
                // Set form back to initial "at rest" state
                self.authorizedChanges = false;

                // TODO: show confirmation message or something here?
            }
        });
    };

    this.toggleAuthorizedChanges = function () {
        var self = this;

        self.authorizedChanges = !self.authorizedChanges;
    };

    this.setAccountType = function (acct, acctType) {
        acct.accountType = acctType;
    };

    var fetchDisclaimer = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getDisclaimerText'}, {query: {method:'GET', isArray:false}});

    this.disclaimer;
    this.initDisclaimer = function () {
        var self = this;
        
        fetchDisclaimer.query().$promise.then(function (response) {
            if(response.failure) {
                notificationCenterService.displayNotifications('directDeposit.invalid.missing.disclaimer', "error");
            }
            else {
                self.disclaimer = response.disclaimer;
            }
        });
    };
    this.initDisclaimer();

}]);
