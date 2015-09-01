/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

modules = {

    'angular' {
        resource url:[file: 'js/angular/angular.min.js']
        resource url:[file: 'js/angular/angular-resource.min.js']
        resource url:[file: 'js/angular/angular-route.min.js']
        resource url:[file: 'js/angular/angular-sanitize.min.js']
        resource url:[file: 'js/angular/angular-animate.min.js']
        resource url:[file: 'js/angular/angular-ui-router.min.js']
        resource url:[file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
        resource url:[file: 'js/angular/lrInfiniteScroll.js']
        resource url:[plugin: 'banner-ui-ss', file: 'js/moment.js']
        resource url:[plugin: 'banner-ui-ss', file: 'js/angular/angular-common.js']
    }

   'generalSsbApp' {
       dependsOn "angular,bootstrap,bannerSelfService,glyphicons"

       defaultBundle environment == "development" ? false : "generalSsbApp"

       //Main configuration file
       resource url: [file: 'generalSsbApp/app.js']

       // Services
       resource url:[file: 'generalSsbApp/ddListing/ddListing-service.js']
       resource url:[file: 'generalSsbApp/common/services/breadcrumb-service.js']

       // Controllers
       resource url:[file: 'generalSsbApp/ddListing/ddListing-controller.js']

       // Filters
       resource url:[file: 'generalSsbApp/common/filters/i18n-filter.js']

       // Directives

       // CSS

   }

}
