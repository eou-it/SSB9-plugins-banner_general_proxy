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
        if(!$scope.account.bankRoutingNum){
            $scope.routingNumErr = true;
            $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.missing.routing.number');
            $scope.account.bankName = null;
        }
        else
        {
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
    
    $scope.setAccountType = function (acctType) {
        $scope.account.accountType = acctType;
    };
    
    $scope.toggleAuthorizedChanges = function () {
        $scope.authorizedChanges = !$scope.authorizedChanges;
    };
    
    $scope.saveAccount = function() {
        if(!$scope.account.bankRoutingNum) {
            $scope.routingNumErr = true;
            $scope.routingNumMessage = $filter('i18n')('directDeposit.invalid.missing.routing.number');
            $scope.account.bankName = null;
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
