/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

globalProxyManagementApp.service( 'breadcrumbService', ['$filter', '$rootScope', function ($filter, $rootScope) {
    let constantBreadCrumb = [],
        callingUrl,
        globalProxyUrl,
        CALLING_URL = 1,
        GEN_LANDING_PAGE_SIGNATURE,
        GLOBAL_PROXY_PAGE_URL;

    $rootScope.applicationContextRoot = $('meta[name=applicationContextRoot]').attr("content");
    GEN_LANDING_PAGE_SIGNATURE = new RegExp($rootScope.applicationContextRoot +'/ssb/general$');
    GLOBAL_PROXY_PAGE_URL = $rootScope.applicationContextRoot  + '/ssb/globalProxy' + '#';

    this.reset = function() {
        let label;
        globalProxyUrl = sessionStorage.getItem('globalProxyInfoCallingPage');
        constantBreadCrumb = [
            {
                label: 'general.breadcrumb.globalProxyManagement',
                url: globalProxyUrl ? GLOBAL_PROXY_PAGE_URL : ''
            }
        ];

        callingUrl = sessionStorage.getItem('genAppCallingPage') + '#';

        if (callingUrl) {
            label = GEN_LANDING_PAGE_SIGNATURE.test(callingUrl) ? 'banner.generalssb.landingpage.title' : 'default.paginate.prev';

            constantBreadCrumb.splice(0, 0, {
                label: label,
                url: CALLING_URL
            });
        }
    };

    this.setBreadcrumbs = function (bc) {
        this.reset();
        constantBreadCrumb.push.apply(constantBreadCrumb, bc);
        if (bc[0]){
            window.document.title = $filter('i18n')(bc[0].label);
        }
    };

    this.refreshBreadcrumbs = function() {
        let breadCrumbInputData = {},
            updatedHeaderAttributes,
            backButtonUrl = '',
            registerBackButtonClickListenerOverride = function(location) {
                $('#breadcrumbBackButton').on('click keypress',function(){
                    window.location = location;
                })
            };

        let addTabIndexToBackButton = function(){
            $('#breadcrumbBackButton').attr('tabindex','0');
        };

        _.each (constantBreadCrumb, function(item) {
            let label = ($filter('i18n')(item.label));

            if (item.url) {
                if (item.url === CALLING_URL) {
                    breadCrumbInputData[label] = callingUrl;
                    backButtonUrl = callingUrl;
                } else {
                    breadCrumbInputData[label] = "/" + document.location.pathname.slice(Application.getApplicationPath().length + 1) + "#" + item.url;
                    backButtonUrl = item.url;
                }
            } else {
                breadCrumbInputData[label] = "";
            }
        });

        updatedHeaderAttributes = {
            "breadcrumb":breadCrumbInputData
        };

        BreadCrumbAndPageTitle.draw(updatedHeaderAttributes);

        // As this is in a consolidated app, the default "previous breadcrumb" logic needs to be overridden to
        // point the back button to the calling page URL.  (Note that the back button is only used for mobile,
        // not desktop.)
        addTabIndexToBackButton();
        registerBackButtonClickListenerOverride(backButtonUrl);
    };
}]);
