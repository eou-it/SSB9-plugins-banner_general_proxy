/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service( 'breadcrumbService', ['$filter',function ($filter) {
    var constantBreadCrumb = [];
    var list = [];
    var appUrl = "";
    this.reset = function() {
        constantBreadCrumb = [
            {
                //TODO: may need to be redefined for Direct Deposit
                label: 'general.breadcrumb.bannerSelfService',
                url: '/'
            },
            {
                //TODO: may need to be redefined for Direct Deposit
                label: 'general.breadcrumb.generalSsb',
                url: '/directDepositApp1'
            }
        ];
        list = [];
        appUrl = document.location.origin + document.location.pathname + "#";
    };
    this.setBreadcrumbs = function (bc) {
        this.reset();
        constantBreadCrumb.push.apply(constantBreadCrumb, bc);
        if (constantBreadCrumb) {
            _.each(constantBreadCrumb, function (breadcrumb) {
                list.push($filter('i18n')(breadcrumb.label));
            });
        }
    };
    this.refreshBreadcrumbs = function() {
        $('#homeArrow').attr('href', appUrl);
        var breadCrumbInputData = {};
        _.each (constantBreadCrumb, function(item) {
            var label = ($filter('i18n')(item.label));
            if (item.url) {
                breadCrumbInputData[label]="/" + document.location.pathname.slice(Application.getApplicationPath().length+1) + "#"+item.url;
            } else {
                breadCrumbInputData[label] = "";
            }
        });

        var updatedHeaderAttributes = {
            "breadcrumb":breadCrumbInputData
        };

        BreadCrumbAndPageTitle.draw(updatedHeaderAttributes);
    };
}]);