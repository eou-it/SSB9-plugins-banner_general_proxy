/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('directDepositService', ['$resource', function ($resource) {

    var fetchDisclaimer = $resource('../ssb/:controller/:action',
            {controller: 'UpdateAccount', action: 'getDisclaimerText'}, {query: {method:'GET', isArray:false}}),
        fetchRoles = $resource('../ssb/:controller/:action',
            {controller: 'General', action: 'getRoles'}, {query: {method:'GET', isArray:false}}),
        fetchConfig = $resource('../ssb/:controller/:action',
            {controller: 'DirectDepositConfiguration', action: 'getConfig'}, {query: {method:'GET', isArray:true}});

    this.getDisclaimer = function () {
        return fetchDisclaimer.query();
    };
    
    this.getRoles = function () {
        return fetchRoles.query();
    };

    this.getConfiguration = function () {
        return fetchConfig.query();
    };

    // Destroy all popovers (i.e. Bootstrap popovers)
    this.destroyAllPopovers = function (){
        // When created, the actual popover is the next sibling adjacent to the
        // AngularJS popover element.  The actual popover has the '.popover.in'
        // CSS selector.  Here's a diagram:
        //
        //     ANGULARJS ELEMENT        ACTUAL POPOVER VISIBLE TO USER
        //     <pop-over ...></pop-over><div class="popover in" ...> ... </div>
        //
        // Thus the previous sibling (grabbed with prev()) is the
        // AngularJS popover element that needs to have 'destroy' called on it.
        $('body').find('.popover.in').prev().popover('destroy');
    }

}]);