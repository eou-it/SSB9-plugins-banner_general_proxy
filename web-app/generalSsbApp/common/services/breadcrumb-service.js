/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service( 'breadcrumbService', ['$filter',function ($filter) {
    var constantBreadCrumb = [];

    this.reset = function() {
        constantBreadCrumb = [
            {
                label: 'banner.generalssb.landingpage.title',
                url: '/'
            }
        ];
    };

    this.setBreadcrumbs = function (bc) {
        this.reset();
        constantBreadCrumb.push.apply(constantBreadCrumb, bc);
    };

    this.refreshBreadcrumbs = function() {
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