/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('ddEditAccountController', ['$scope', '$modalInstance', '$state', '$filter', 'ddEditAccountService', 'notificationCenterService',
    function($scope, $modalInstance, $state, $filter, ddEditAccountService, notificationCenterService){

    $scope.creatingNewAccount = false;
    $scope.authorizedChanges = false;

    $scope.editAccountService = ddEditAccountService;

    $scope.routingNumErr = false;
    $scope.routingNumMessage;

    $scope.validateRoutingNum = function () {
        if($scope.account.bankRoutingInfo.bankRoutingNum){
            ddEditAccountService.getBankInfo($scope.account.bankRoutingInfo.bankRoutingNum).$promise.then(function (response) {
                if(response.failure) {
                    $scope.routingNumErr = true;
                    $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.routing.number');
                    $scope.account.bankRoutingInfo.bankName = null;
                    notificationCenterService.displayNotifications($scope.routingNumMessage, "error");
                }
                else{
                    $scope.routingNumMessage = response.bankName;
                    $scope.routingNumErr = false;
                    notificationCenterService.clearNotifications();
                }
            });
        }
    };
    
    $scope.accountNumErr = false;
    $scope.accountNumMessage;
    
    $scope.validateAccountNum = function () {
        if($scope.account.bankAccountNum){
            ddEditAccountService.validateAccountNum($scope.account.bankAccountNum).$promise.then(function (response) {
                if(response.failure) {
                    $scope.accountNumErr = true;
                    $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.account.number');
                    notificationCenterService.displayNotifications($scope.accountNumMessage, "error");
                }
                else{
                    $scope.accountNumMessage = null;
                    $scope.accountNumErr = false;
                    notificationCenterService.clearNotifications();
                }
            });
        }
    };
    
    $scope.accountTypeErr = false;
    
    $scope.setAccountType = function (acctType) {
        $scope.account.accountType = acctType;
        $scope.accountTypeErr = false;
        notificationCenterService.clearNotifications();
    };
    
    $scope.toggleAuthorizedChanges = function () {
        $scope.authorizedChanges = !$scope.authorizedChanges;
    };
    
    $scope.saveAccount = function() {
        if(requiredFieldsValid()) {
            ddEditAccountService.saveApAccount($scope.account, $scope.creatingNewAccount).$promise.then(function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                }
                else {
                    $state.go('directDepositApp1', {}, {reload: true, inherit: false, notify: true});
                    $scope.cancelModal();
                }
            });
        }
    };
    
    var requiredFieldsValid = function() {
        if(!$scope.account.bankRoutingInfo.bankRoutingNum){
            $scope.routingNumErr = true;
            $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.missing.routing.number');
            $scope.account.bankRoutingInfo.bankName = null;
            notificationCenterService.displayNotifications($scope.routingNumMessage, "error");
        } 
        else if($scope.routingNumErr){
            notificationCenterService.displayNotifications($scope.routingNumMessage, "error");
        }
        
        if(!$scope.account.bankAccountNum) {
            $scope.accountNumErr = true;
            $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.missing.account.number');
            notificationCenterService.displayNotifications($scope.accountNumMessage, "error");
        } 
        else if($scope.accountNumErr){
            notificationCenterService.displayNotifications($scope.accountNumMessage, "error");
        }
        
        if(!$scope.account.accountType) {
            $scope.accountTypeErr = true;
            notificationCenterService.displayNotifications('directDeposit.invalid.missing.account.type', "error");
        }
        
        return !($scope.routingNumErr || $scope.accountNumErr || $scope.accountTypeErr);
    };
    
    $scope.cancelModal = function () {
        $modalInstance.dismiss('cancel');
        notificationCenterService.clearNotifications();
    };

    this.init = function() {
        // In initializing this controller, we could be doing an account create or account edit.  For the former, no
        // account will exist and we need to create a new account object.  For the latter, an account will already
        // exist on scope, so use that.  (At the time of this writing, the latter case happens only when $modal.open()
        // is called, initializing this controller with a parent scope object.)
        if (!$scope.account) {
            // Create "new account" object
            $scope.account = {
                pidm: null,
                status: null,
                apIndicator: 'A',
                hrIndicator: 'I',
                bankAccountNum: null,
                amount: null,
                percent: 100,
                accountType: '',
                bankRoutingInfo: {
                    bankRoutingNum: null
                }
            };

            $scope.creatingNewAccount = true;
        }
    };


    // INITIALIZE
    // ----------
    this.init();

}]);
