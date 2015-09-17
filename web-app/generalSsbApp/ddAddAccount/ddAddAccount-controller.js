/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('DdAddAccountController', ['$scope', '$modalInstance', '$state', 'ddAddAccountService', function($scope, $modalInstance, $state, ddAddAccountService){

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
    
    $scope.authorizedChanges = false;
    
    $scope.setAccountType = function (acctType) {
        $scope.account.accountType = acctType;
    };
    
    $scope.toggleAuthorizedChanges = function () {
        $scope.authorizedChanges = !$scope.authorizedChanges;
    }
    
    $scope.saveAccount = function() {
        if(!$scope.account.bankRoutingNum) {
            alert("inp1 error");
        } else if(!$scope.account.bankAccountNum) {
            alert("inp2 error");
        } else if(!$scope.account.accountType) {
            alert("inp3 error");
        } else if(!$scope.authorizedChanges) {
            alert("disclaimer error");
        } else {
            ddAddAccountService.createApAccount($scope.account).$promise.then(function (response) {
                if(response.failure) {
                    alert("response error");
                }
                else {
                	$state.go('directDepositApp1', {}, {reload: true, inherit: false, notify: true});
                    $modalInstance.dismiss('cancel');
                }
            });
        }
    };
    
    $scope.cancelModal = function () {
        $modalInstance.dismiss('cancel');
    };
    
}]);
