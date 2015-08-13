/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

modules = {

    'angular' {
        resource url:[file: 'js/angular/angular.min.js']
        resource url:[file: 'js/angular/angular-resource.min.js']
        resource url:[file: 'js/angular/angular-route.js']
        resource url:[file: 'js/angular/angular-sanitize.min.js']
        resource url:[file: 'js/angular/angular-animate.min.js']
        resource url:[file: 'js/angular/angular-ui-router.min.js']
        resource url:[file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js']
        resource url:[file: 'js/angular/lrInfiniteScroll.js']
        resource url:[plugin: 'banner-ui-ss', file: 'js/moment.js']
        resource url:[plugin: 'banner-ui-ss', file: 'js/angular/angular-common.js']
    }

   'directDepositApp' {
       dependsOn "angular,bootstrap,bannerSelfService,glyphicons"

       defaultBundle environment == "development" ? false : "directDepositApp"

       //Main configuration file
       resource url: [file: 'directDepositApp/app.js']
   }

}
