/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service( 'breadcrumbService', ['$filter', '$cookies', function ($filter, $cookies) {
    var constantBreadCrumb = [],
        callingUrl,
        CALLING_URL = 1;

    this.reset = function() {
        constantBreadCrumb = [
            {
                label: 'banner.generalssb.landingpage.title',
                url: '/'
            }
        ];

        callingUrl = $cookies.get('generalCallingPage');

        if (callingUrl) {
            constantBreadCrumb.splice(0, 0, {
                label: 'default.paginate.prev',
                url: CALLING_URL
            });
        }
    };

    this.setBreadcrumbs = function (bc) {
        this.reset();
        constantBreadCrumb.push.apply(constantBreadCrumb, bc);
    };

    this.refreshBreadcrumbs = function() {
        var breadCrumbInputData = {},
            updatedHeaderAttributes,
            registerBackButtonClickListenerOverride = function(location) {
                $('#breadcrumbBackButton').on('click',function(){
                    window.location = location;
                })
            };

        _.each (constantBreadCrumb, function(item) {
            var label = ($filter('i18n')(item.label));
            if (item.url) {
                breadCrumbInputData[label] = (item.url === CALLING_URL) ? callingUrl :
                    "/" + document.location.pathname.slice(Application.getApplicationPath().length+1) + "#"+item.url;
            } else {
                breadCrumbInputData[label] = "";
            }
        });

        updatedHeaderAttributes = {
            "breadcrumb":breadCrumbInputData
        };

        BreadCrumbAndPageTitle.draw(updatedHeaderAttributes);

        // As this app's breadcrumb service has the capability to point back to the original calling page, the
        // default "previous breadcrumb" logic needs to be overridden to point the back button to the calling
        // page URL.  (Note that the back button is only used for mobile, not desktop.)
        registerBackButtonClickListenerOverride(callingUrl);
    };
}]);