/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {
    /* Override UI Bootstrap 0.10.0 to use version 0.13.3
     * Override AngularUI Router 0.2.10 to use version 0.2.15 */
    overrides {
        'angularApp' {
            resource id:[plugin: 'banner-ui-ss',file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js'], url: [file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
            resource id:[plugin: 'banner-ui-ss',file: 'js/angular/angular-ui-router.min.js'], url: [file: 'js/angular/angular-ui-router.min.js']
        }
    }

    'angularGeneral' {
        resource url:[plugin: 'banner-ui-ss', file: 'js/angular/angular-route.min.js']

    }

    'bootstrapLTR' {
        dependsOn "jquery"
        defaultBundle environment == "development" ? false : "bootstrap"

        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/css/bootstrap.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'css/bootstrap-fixes.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/js/bootstrap.js']
    }

    'bootstrapRTL' {
        dependsOn "jquery"
        defaultBundle environment == "development" ? false : "bootstrap"

        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/css/bootstrap-rtl.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'css/bootstrap-fixes-rtl.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/js/bootstrap.js']
    }

    'generalSsbApp' {
        dependsOn "angularGeneral,glyphicons"

        defaultBundle environment == "development" ? false : "generalSsbApp"

        //Main configuration file
        resource url: [file: 'generalSsbApp/app.js']

        // Services
        resource url:[file: 'generalSsbApp/common/services/breadcrumb-service.js']
        resource url:[file: 'generalSsbApp/common/services/generalSsb-service.js']

        // Controllers
        resource url:[file: 'generalSsbApp/landingPage/gssLandingPage-controller.js']

        // Filters
        resource url:[file: 'generalSsbApp/common/filters/i18n-filter.js']

        // Directives
        resource url:[file: 'generalSsbApp/landingPage/gssLandingPage-directive.js']

    }

    'generalSsbAppLTR' {
        dependsOn "bannerWebLTR, generalSsbApp, bootstrapLTR"

        // CSS
        resource url:[file: 'css/generalSsbMain.css'],   attrs: [media: 'screen, projection']
        resource url:[file: 'css/generalSsbResponsive.css'],   attrs: [media: 'screen, projection']
    }

    'generalSsbAppRTL' {
        dependsOn "bannerWebRTL, generalSsbApp, bootstrapRTL"

        // CSS
        resource url:[file: 'css/generalSsbMain-rtl.css'],   attrs: [media: 'screen, projection']
        resource url:[file: 'css/generalSsbResponsive-rtl.css'],   attrs: [media: 'screen, projection']
    }

}
