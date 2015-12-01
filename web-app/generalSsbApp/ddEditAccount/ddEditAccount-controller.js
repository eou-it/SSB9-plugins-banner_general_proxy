/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('ddEditAccountController', ['$scope', '$modalInstance', '$state', '$filter', 'ddEditAccountService', 'notificationCenterService', 'editAcctProperties',
    function($scope, $modalInstance, $state, $filter, ddEditAccountService, notificationCenterService, editAcctProperties){

    $scope.typeIndicator = editAcctProperties.typeIndicator;
    $scope.creatingNewAccount = editAcctProperties.creatingNew;
    $scope.authorizedChanges = false;

    $scope.popoverElements = {}; // Used to coordinate popovers in modal

    //routing and account number should only contain upper case letters and digits
    var invalidCharRegEx = /[^A-Za-z0-9]/i;
    $scope.routingNumErr = false;
    $scope.routingNumMessage;

    $scope.validateRoutingNum = function () {
        if($scope.account.bankRoutingInfo.bankRoutingNum){

            if( invalidCharRegEx.test($scope.account.bankRoutingInfo.bankRoutingNum) ){
                $scope.routingNumErr = true;
                $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.chars.routing');
                $scope.account.bankRoutingInfo.bankName = null;
                notificationCenterService.displayNotifications($scope.routingNumMessage, "error");
            }
            else {
                $scope.account.bankRoutingInfo.bankRoutingNum = $scope.account.bankRoutingInfo.bankRoutingNum.toUpperCase();

                ddEditAccountService.getBankInfo($scope.account.bankRoutingInfo.bankRoutingNum).$promise.then(function (response) {
                    if(response.failure) {
                        $scope.routingNumErr = true;
                        $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.routing.number');
                        $scope.account.bankRoutingInfo.bankName = null;
                        notificationCenterService.displayNotifications($scope.routingNumMessage, "error");
                    }
                    else {
                        $scope.routingNumMessage = response.bankName;
                        $scope.routingNumErr = false;
                        notificationCenterService.clearNotifications();
                    }
                });
            }
        }
    };

    $scope.accountNumErr = false;
    $scope.accountNumMessage;
    
    $scope.validateAccountNum = function () {
        if($scope.account.bankAccountNum){

            if( invalidCharRegEx.test($scope.account.bankAccountNum) ){
                $scope.accountNumErr = true;
                $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.chars.account');
                notificationCenterService.displayNotifications($scope.accountNumMessage, "error");
            }
            else {
                $scope.account.bankAccountNum = $scope.account.bankAccountNum.toUpperCase();

                ddEditAccountService.validateAccountNum($scope.account.bankAccountNum).$promise.then(function (response) {
                    if(response.failure) {
                        $scope.accountNumErr = true;
                        $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.account.number');
                        notificationCenterService.displayNotifications($scope.accountNumMessage, "error");
                    }
                    else {
                        $scope.accountNumMessage = null;
                        $scope.accountNumErr = false;
                        notificationCenterService.clearNotifications();
                    }
                });
            }
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
        if($scope.typeIndicator === 'HR'){
        	ddEditAccountService.setAmountValues($scope.account, $scope.amount.type);
        }

        if(requiredFieldsValid()) {
            ddEditAccountService.saveAccount($scope.account, $scope.creatingNewAccount).$promise.then(function (response) {
                if(response.failure) {
                    notificationCenterService.displayNotifications(response.message, "error");
                }
                else {
                    $state.go('directDepositListing', {}, {reload: true, inherit: false, notify: true});
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
    
    
    var handleAmounts = function(){
    	if($scope.amount.type === 'remaining'){
            $scope.account.percent = 100;
            $scope.account.amount = ''; // grails will ignore null values, so use empty strings instead
    	}
    	else if($scope.amount.type === 'amount'){
    		$scope.account.percent = '';
        	//$scope.account.amount = $scope.amount.val;
    	}
    	else if($scope.amount.type === 'percentage'){
    		//$scope.account.percent = $scope.amount.val;
            $scope.account.amount = '';
    	}
    };/*
    ddy=0;
    $scope.showThing1 = function(){
    	if($scope.amount.type === 'percentage'){
    		if(ddy === 0){
    		$scope.amount.val = $scope.account.percent;
    		ddy=1; ddx=0;
    		}
    		return true;
    	}
    	else return false;
    };
    ddx=0;
    $scope.showThing2 = function(){
    	//console.log('amount:'+ x++);
    	if($scope.amount.type === 'amount'){
    		if(ddx === 0){
    		$scope.amount.val = $scope.account.amount;
    		ddx=1; ddy=0;
    		}
    		return true;
    	}
    	else return false;
    };*/
    //ng-focus="amount.val=account.amount"
    //ng-focus="amount.val=account.percent" 
    $scope.cancelModal = function () {
        $modalInstance.dismiss('cancel');
        notificationCenterService.clearNotifications();
    };

    this.init = function() {
        // In initializing this controller, we could be doing an account create, edit, or delete.  For the create, no
        // account will exist and we need to instantiate a new account object.  For the edit and delete, an account will
        // already exist on scope, so use that.  (At the time of this writing, the edit and delete cases happen only
        // when $modal.open() is called, initializing this controller with a parent scope object.)
        if ($scope.creatingNewAccount) {
            // Create "new account" object
            $scope.account = {
                pidm: null,
                status: null,
                apIndicator: 'A',
                hrIndicator: 'I',
                bankAccountNum: null,
                amount: null,
                percent: null,
                accountType: '',
                bankRoutingInfo: {
                    bankRoutingNum: null
                }
            };

            if($scope.typeIndicator === 'HR'){
                $scope.account.hrIndicator = 'A';
                $scope.account.apIndicator = 'I';
            }
        }
        else {
	        if($scope.typeIndicator === 'HR'){
	        	$scope.amount.type = ddEditAccountService.getAmountType($scope.account);
	        }
        }
    };
    
    $scope.amount = {};
    $scope.amount.type = 'remaining';
    /*$scope.amount.val = null;
    var initAmounts = function(){
    	if($scope.account.allocation === 'Remaining'){
    		$scope.amount.type = 'remaining';
    		$scope.account.percent = null;
    		$scope.account.amount = null;
    	}
    	else if($scope.account.amount != null){
    		$scope.amount.type = 'amount';
    		//$scope.amount.val = $scope.account.amount;
    	}
    	else if($scope.account.percent != null){
    		$scope.amount.type = 'percentage';
    		//$scope.amount.val = $scope.account.percent;
    	}
    };*/


    // INITIALIZE
    // ----------
    this.init();

}]);
