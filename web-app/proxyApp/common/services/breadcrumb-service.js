/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyApp.service( 'breadcrumbService', ['$filter',function ($filter) {
    var constantBreadCrumb = [];

    this.reset = function() {
        constantBreadCrumb = [
            {
                label: 'proxy.landingpage.title',
                url: '/'
            }
        ];
    };

    this.setBreadcrumbs = function (bc) {
        this.reset();
        constantBreadCrumb.push.apply(constantBreadCrumb, bc);
    };

    this.refreshBreadcrumbs = function() {
        var updatedHeaderAttributes = {
                "breadcrumb": {}
            };

        _.each (constantBreadCrumb, function(item) {
            var label = ($filter('i18n')(item.label));

            updatedHeaderAttributes.breadcrumb[label] =
                item.url ? ("/" + document.location.pathname.slice(Application.getApplicationPath().length+1) + "#"+item.url) : "";
        });

        BreadCrumbAndPageTitle.draw(updatedHeaderAttributes);
    };
}]);
