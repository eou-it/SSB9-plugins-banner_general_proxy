/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('DdAddAccountController', ['$scope', 'ddAddAccountService', function($scope, ddAddAccountService){
	
	$scope.account = {};
	$scope.madeit = 'here we go..';
	
	$scope.account.pidm;
	$scope.account.status;
	$scope.account.apIndicator = 'A';
	$scope.account.hrIndicator = 'I';
	$scope.account.bankAccountNum;
	$scope.account.bankRoutingNum;
	$scope.account.amount;
	$scope.account.percent = 100;
	$scope.account.accountType;
	
	$scope.saveAccount = function() {
		if(!$scope.account.bankRoutingNum) {
			alert("inp1 error");
		} else if(!$scope.account.bankAccountNum) {
			alert("inp2 error");
		} else if(!$scope.account.accountType) {
			alert("inp3 error");
		} else {
			ddAddAccountService.createApAccount($scope.account).$promise.then(function (response) {
				if(response.failure) {
					alert("response error");
				}
				else {
					$scope.madeit = "Good!";
				}
			});
		}
	};
}]);
