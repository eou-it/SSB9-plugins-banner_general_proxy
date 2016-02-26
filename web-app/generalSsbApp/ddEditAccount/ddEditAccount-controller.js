/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppControllers.controller('ddEditAccountController', ['$scope', '$modalInstance', '$state', '$filter', '$timeout', 'directDepositService', 'ddEditAccountService', 'ddListingService', 'notificationCenterService', 'editAcctProperties',
    function($scope, $modalInstance, $state, $filter, $timeout, directDepositService, ddEditAccountService, ddListingService, notificationCenterService, editAcctProperties){

        $scope.typeIndicator = editAcctProperties.typeIndicator;
        $scope.creatingNewAccount = editAcctProperties.creatingNew;

        $scope.routNumFocused = false;
        $scope.acctNumFocused = false;
        $scope.acctTypeFocused = false;
        $scope.amountAmtFocused = false;
        $scope.amountPctFocused = false;
        $scope.dropdownIsOpen = false;
        $scope.isDisplayPriority = true;

        $scope.popoverElements = {}; // Used to coordinate popovers in modal

        //routing and account number should only contain upper case letters, digits, or the allowed special characters ^!_@#$%&,*:./+-
        var invalidCharRegEx = /[^A-Za-z0-9\^!_@#\$%&,\*:\./\+-]/i;
        $scope.routingNumErr = false;
        $scope.routingNumMessage;

        var routingNotification = null,

            setRoutingNumError = function (message) {
                notificationCenterService.removeNotification(routingNotification);

                $scope.routingNumErr = true;
                $scope.routingNumMessage = message;
                $scope.account.bankRoutingInfo.bankName = null;
                routingNotification = notificationCenterService.addNotification($scope.routingNumMessage, "error");
                clearMiscMessage();
            };

        $scope.validateRoutingNum = function () {
            if($scope.account.bankRoutingInfo.bankRoutingNum){

                if( invalidCharRegEx.test($scope.account.bankRoutingInfo.bankRoutingNum) ){
                    setRoutingNumError($filter('i18n')('directDeposit.invalid.chars.routing'));
                }
                else {
                    $scope.account.bankRoutingInfo.bankRoutingNum = $scope.account.bankRoutingInfo.bankRoutingNum.toUpperCase();

                    ddEditAccountService.getBankInfo($scope.account.bankRoutingInfo.bankRoutingNum).$promise.then(function (response) {
                        if(response.failure) {
                            setRoutingNumError($filter('i18n')('directDeposit.invalid.routing.number'));
                        }
                        else {
                            $scope.account.bankRoutingInfo.bankName = response.bankName;
                            $scope.routingNumErr = false;
                            notificationCenterService.removeNotification(routingNotification);
                        }
                    });
                }
            }
            else {
                $scope.account.bankRoutingInfo.bankName = null;
            }
        };

        $scope.accountNumErr = false;
        $scope.accountNumMessage;

        var accountNotification = null,

            setAccountNumError = function (message) {
                notificationCenterService.removeNotification(accountNotification);

                $scope.accountNumErr = true;
                $scope.accountNumMessage = message
                accountNotification = notificationCenterService.addNotification($scope.accountNumMessage, "error");
                clearMiscMessage();
            };

        $scope.validateAccountNum = function () {
            if($scope.account.bankAccountNum){

                if( invalidCharRegEx.test($scope.account.bankAccountNum) ){
                    setAccountNumError($filter('i18n')('directDeposit.invalid.chars.account'));
                }
                else {
                    $scope.account.bankAccountNum = $scope.account.bankAccountNum.toUpperCase();

                    ddEditAccountService.validateAccountNum($scope.account.bankAccountNum).$promise.then(function (response) {
                        if(response.failure) {
                            setAccountNumError($filter('i18n')('directDeposit.invalid.account.number'));
                        }
                        else {
                            $scope.accountNumMessage = null;
                            $scope.accountNumErr = false;
                            notificationCenterService.removeNotification(accountNotification);
                        }
                    });
                }
            }
        };

        $scope.accountTypeErr = false;

        var typeNotification = null;

        $scope.setAccountType = function (acctType) {
            $scope.account.accountType = acctType;
            $scope.accountTypeErr = false;
            notificationCenterService.removeNotification(typeNotification);
        };

        $scope.checkBrowserLocale = function(localeIn) {
            var locale = $('meta[name=locale]').attr("content");
            if (localeIn.toUpperCase() === locale.toUpperCase() ) {
                return true;
            } else {
                return false;
            }
        };

        $scope.setAccountPriority = function (priority) {
            if($scope.account.priority != priority) {
                if($scope.creatingNewAccount){
                    ddEditAccountService.doReorder = 'new';
                    $scope.account.priority = priority;
                }
                else {
                    ddEditAccountService.doReorder = 'single';
                    $scope.account.priority = priority;
                }
            }
        };

        $scope.selectOtherAcct = function (acct) {
            $scope.otherAccountSelected = acct;
        };

        $scope.isRemaining = function(){
            return directDepositService.isRemaining($scope.account);
        };

        $scope.showReprioritizeRemainingMessage = function() {
            $scope.displayReprioritizeRemainingWarning();

            $scope.miscMessage = $filter('i18n')('directDeposit.invalid.reprioritze.remaining');
        };

        var displayMiscError = function (msg) {
            $scope.miscMessage = msg;
        };

        var clearMiscMessage = function () {
            $scope.miscMessage = null;
        };

        var resetAccountPriority = function () {
            _.find(ddEditAccountService.priorities, function(priorityObj) {
                if(priorityObj.persistVal === $scope.account.priority){
                    $scope.account.priority = priorityObj.displayVal;
                    return true;
                }
            });
        };

        var validateAmounts = function () {
            var result = ddListingService.validateAmountForAccount($scope, $scope.account);

            if(result && $scope.isRemaining()){
                //move to last position
                $scope.setAccountPriority($scope.priorities[$scope.priorities.length-1].displayVal);
            }

            if(!result) {
                clearMiscMessage();
            }
            else {
                $scope.amountErr = false;
                $scope.amountMessage = null;
            }

            return result;
        };

        $scope.saveAccount = function() {
            var doSave = true,
                notifications = [],
                doStateGoSuccess = function() {
                    notifications.push({message: 'default.save.success.message',
                        messageType: $scope.notificationSuccessType,
                        flashType: $scope.flashNotification});

                    $state.go('directDepositListing',
                        {onLoadNotifications: notifications},
                        {reload: true, inherit: false, notify: true}
                    );
                };

            $scope.setup.authorizedChanges = false;

            if($scope.setup.createFromExisting === 'yes'){
                $scope.account.bankAccountNum = $scope.otherAccountSelected.bankAccountNum;
                $scope.account.bankRoutingInfo = $scope.otherAccountSelected.bankRoutingInfo;
                $scope.account.accountType = $scope.otherAccountSelected.accountType;
            }
            else {
                doSave = requiredFieldsValid();
            }

            if($scope.typeIndicator === 'HR'){
                doSave = validateAmounts() && doSave;
                ddEditAccountService.setAmountValues($scope.account, $scope.account.amountType);
            }

            if(doSave) {

               if(ddEditAccountService.doReorder === 'single'){
                    // Temporarily hide priority during this transition. This is because the persisted priority,
                    // often different from that displayed to the user, will be set in the account object to save
                    // it to the database. However, this results in its being briefly displayed to the user,
                    // which could be disconcerting.
                    $scope.isDisplayPriority = false;

                    ddEditAccountService.reorderAccounts($scope.account).$promise.then(function (response) {
                        $scope.isDisplayPriority = true; // Set priority display back to normal state

                        if(response[0].failure) {
                            notificationCenterService.displayNotification(response[0].message, "error");
                            displayMiscError(response[0].message);

                            resetAccountPriority();
                        }
                        else {
                            ddEditAccountService.doReorder = false;

                            doStateGoSuccess();
                        }
                    });
                }
                else {
                    ddEditAccountService.saveAccount($scope.account, $scope.creatingNewAccount).$promise.then(function (response) {
                        if(response.failure) {
                            notificationCenterService.displayNotification(response.message, "error");
                            displayMiscError(response.message);

                            if($scope.typeIndicator === 'HR'){
                                resetAccountPriority();
                            }

                            // if there is an error when creating from existing account, then reset account
                            // so user can start fresh
                            if($scope.setup.createFromExisting === 'yes'){
                                $scope.account.bankAccountNum = null;
                                $scope.account.bankRoutingInfo = {bankRoutingNum: null};
                                $scope.account.accountType = null;
                            }
                        }
                        else {
                            doStateGoSuccess();
                        }
                    });
                }
            }
            else {
                // if inputs are not valid when creating from existing account, then reset account
                // so user can start fresh
                if($scope.setup.createFromExisting === 'yes'){
                    $scope.account.bankAccountNum = null;
                    $scope.account.bankRoutingInfo = {bankRoutingNum: null};
                    $scope.account.accountType = null;
                }
            }
        };

        var requiredFieldsValid = function() {
            if(!$scope.account.bankRoutingInfo.bankRoutingNum){
                setRoutingNumError($filter('i18n')('directDeposit.invalid.missing.routing.number'));
            }
            else if($scope.routingNumErr){
                notificationCenterService.removeNotification(routingNotification);
                routingNotification = notificationCenterService.addNotification($scope.routingNumMessage, "error");
            }

            if(!$scope.account.bankAccountNum) {
                setAccountNumError($filter('i18n')('directDeposit.invalid.missing.account.number'));
            }
            else if($scope.accountNumErr){
                notificationCenterService.removeNotification(accountNotification);
                accountNotification = notificationCenterService.addNotification($scope.accountNumMessage, "error");
            }

            if(!$scope.account.accountType) {
                notificationCenterService.removeNotification(typeNotification);

                $scope.accountTypeErr = true;
                typeNotification = notificationCenterService.addNotification('directDeposit.invalid.missing.account.type', "error");
                clearMiscMessage();
            }

            return !($scope.routingNumErr || $scope.accountNumErr || $scope.accountTypeErr);
        };

        var clearErrors = function () {
            $scope.routingNumErr = false;
            $scope.routingNumMessage = null;

            $scope.accountNumErr = false;
            $scope.accountNumMessage = null;

            $scope.accountTypeErr = false;

            $scope.amountErr = false;
            $scope.amountMessage = null;

            clearMiscMessage();
        };

        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        this.init = function() {
            $scope.setup = {}
            $scope.setup.hasOtherAccounts = false;

            // start with a 'fresh' reorder flag
            ddEditAccountService.doReorder = false;
            $scope.priorities = ddEditAccountService.priorities;

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
                    percent: 100,
                    accountType: '',
                    bankRoutingInfo: {
                        bankRoutingNum: null
                    },
                    amountType: 'remaining'
                };

                $scope.setup.hasOtherAccounts = editAcctProperties.otherAccounts.length > 0;
                $scope.setup.otherAccounts = editAcctProperties.otherAccounts;
                $scope.setup.createFromExisting;

                $scope.setup.authorizedChanges = false;

                if($scope.typeIndicator === 'HR'){
                    $scope.account.hrIndicator = 'A';
                    $scope.account.apIndicator = 'I';
                    $scope.account.percent = null; // we will determine what value this should be on save, AP is always 100

                    if($scope.setup.hasOtherAccounts){
                        $scope.selectOtherAcct($scope.setup.otherAccounts[0]);
                    }

                    $scope.priorities = angular.copy(ddEditAccountService.priorities);
                    $scope.priorities.push({displayVal: $scope.priorities.length+1, persistVal: null});

                    $scope.account.priority = $scope.priorities[$scope.priorities.length-1].displayVal;
                }

                // clear the errors and disclaimer if user switches from Create From Existing to Create New and vice versa
                $scope.$watch('setup.createFromExisting', function(){
                    clearErrors();
                    $scope.setup.authorizedChanges = false;
                });
            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
