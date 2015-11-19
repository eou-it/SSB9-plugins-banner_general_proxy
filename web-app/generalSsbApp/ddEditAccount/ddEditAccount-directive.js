/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppDirectives.directive('chooseAccount',[function () {
    return{
        restrict: 'E',
        template: "{{( account.accountType === 'C' ? 'directDeposit.account.type.checking' : " +
                    " ( account.accountType === 'S' ? 'directDeposit.account.type.savings' : 'directDeposit.account.type.select'))|i18n}}"
    };
}]);

generalSsbAppDirectives.directive('titleForEditModal',[function () {
    return{
        restrict: 'E',
        template: "{{(creatingNewAccount === true ? 'directDeposit.label.add.ApDeposit' : 'directDeposit.label.edit.ApDeposit')|i18n}}"
    };
}]);

generalSsbAppDirectives.directive('editModalSaveButton',[function () {
    return{
        restrict: 'E',
        template: "{{(creatingNewAccount === true ? 'directDeposit.button.save.new' : 'directDeposit.button.save.changes')|i18n}}"
    };
}]);

/* 
 * usage:
 * place dropdown-helper="begin" on the element with the data-toggle attribute so that when user keys to the previous focusable element
 * with shift+tab the dropdown menu is closed. Place dropdown-helper="end" on the last item in the menu so when the user keys to the
 * next focusable element with tab the dropdown menu closes.
 */
generalSsbAppDirectives.directive('dropdownHelper', [function () {
    return {
        restrict: 'A',
        link: function (scope, elem, attrs) {

            elem.bind('keydown', function(event) {
                var code = event.keyCode || event.which;

                if (code === 9) {
                    if(attrs.dropdownHelper === 'begin'){
                        if(event.shiftKey){
                            //close dropdown if it is open when shift+tab off element
                            if(elem.parent().hasClass('open')){
                                elem.dropdown('toggle');
                            }
                        }
                    }
                    else if(attrs.dropdownHelper === 'end'){
                        if(!event.shiftKey){
                            //close dropdown when tab off element
                            elem.dropdown('toggle');
                        }
                    }
                }
            });
        }
    };
}]);

generalSsbAppDirectives.directive('maskInput', ['$filter', function ($filter) {
    return {
        restrict: 'A',
        require: '?ngModel', 
        link: function (scope, elem, attrs, ngModel) {
            
            // do nothing if ng-model is not present
            if (!ngModel) return;
            
            ngModel.$render = function(){
                if(!scope.creatingNewAccount){
                    elem.val($filter('accountNumMask')(ngModel.$modelValue));
                }
            };
        }
    };
}]);

