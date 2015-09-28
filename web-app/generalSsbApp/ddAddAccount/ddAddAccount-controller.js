/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('DdAddAccountController', ['$scope', '$modalInstance', '$state', '$filter', 'ddAddAccountService', function($scope, $modalInstance, $state, $filter, ddAddAccountService){

    $scope.account = {};
    
    $scope.account.pidm;
    $scope.account.status;
    $scope.account.apIndicator = 'A';
    $scope.account.hrIndicator = 'I';
    $scope.account.bankAccountNum;
    $scope.account.bankRoutingNum;
    $scope.account.amount;
    $scope.account.percent = 100;
    $scope.account.accountType = '';
    $scope.account.bankName;

    $scope.authorizedChanges = false;
    
    $scope.routingNumErr = false;
    $scope.routingNumMessage;
    
    $scope.validateRoutingNum = function () {
	    if($scope.account.bankRoutingNum){
		    ddAddAccountService.getBankInfo($scope.account.bankRoutingNum).$promise.then(function (response) {
		        if(response.failure) {
		            $scope.routingNumErr = true;
		            $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.routing.number');
		            $scope.account.bankName = null;
		        }
		        else{
		            $scope.routingNumMessage = response.bankName;
		            $scope.routingNumErr = false;
		        }
		    });
	    }
    };
    
    $scope.accountNumErr = false;
    $scope.accountNumMessage;
    
    $scope.validateAccountNum = function () {
    	if($scope.account.bankAccountNum){
		    ddAddAccountService.validateAccountNum($scope.account.bankAccountNum).$promise.then(function (response) {
		        if(response.failure) {
		            $scope.accountNumErr = true;
		            $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.account.number');
		        }
		        else{
		            $scope.accountNumMessage = null;
		            $scope.accountNumErr = false;
		        }
		    });
    	}
    };
    
    $scope.accountTypeErr = false;
    
    $scope.setAccountType = function (acctType) {
        $scope.account.accountType = acctType;
        $scope.accountTypeErr = false;
    };
    
    $scope.toggleAuthorizedChanges = function () {
        $scope.authorizedChanges = !$scope.authorizedChanges;
    };
    
    $scope.saveAccount = function() {
    	if(requiredFieldsValid()) {
            ddAddAccountService.createApAccount($scope.account).$promise.then(function (response) {
                if(response.failure) {
                    alert(response.message);
                }
                else {
                    $state.go('directDepositApp1', {}, {reload: true, inherit: false, notify: true});
                    $modalInstance.dismiss('cancel');
                }
            });
        }
    };
    
    var requiredFieldsValid = function() {
    	if(!$scope.account.bankRoutingNum){
            $scope.routingNumErr = true;
            $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.missing.routing.number');
            $scope.account.bankName = null;
        }
    	if(!$scope.account.bankAccountNum) {
        	$scope.accountNumErr = true;
            $scope.accountNumMessage = $filter('i18n')('directDeposit.invalid.missing.account.number');
    	}
    	if(!$scope.account.accountType) {
        	$scope.accountTypeErr = true;
        }
    	
    	return !($scope.routingNumErr || $scope.accountNumErr || $scope.accountTypeErr);
    };
    
    $scope.cancelModal = function () {
        $modalInstance.dismiss('cancel');
    };
    
}]);
