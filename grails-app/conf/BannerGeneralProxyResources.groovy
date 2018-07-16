/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {
    /* Override UI Bootstrapui-bootstrap-tpls to use version 0.13.3
     * Override AngularUI Router 0.2.10 to use version 0.2.15 */
    overrides {
        'angularApp' {
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/ui-bootstrap-tpls.min.js'], url: [plugin: 'banner-general-personal-information-ui', file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/angular-ui-router.min.js'], url: [plugin: 'banner-general-personal-information-ui', file: 'js/angular/angular-ui-router.min.js']
        }
    }

    'angular' {
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'js/angular/angular-route.min.js']
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

    'commonComponents' {
        resource url:[file: 'js/xe-components/xe-ui-components.js']
    }
    'commonComponentsLTR' {
        resource url:[file: 'css/xe-components/xe-ui-components.css']
    }
    'commonComponentsRTL' {
        resource url:[file: 'css/xe-components/xe-ui-components-rtl.css']
    }

    'proxyApp' {
        dependsOn "angularGeneral"

        defaultBundle environment == "development" ? false : "proxyApp"

        //Main configuration file
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/app.js']

        // Services
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/common/services/breadcrumb-service.js']
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/common/services/proxyApp-service.js']

        // Controllers
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/proxyAccessHome/gssLandingPage-controller.js']
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/proxyPersonalInfo/proxyPersonalInfo-controller.js']
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/student/proxyViewHolds-controller.js']

        // Filters
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/common/filters/i18n-filter.js']

        // Directives
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/common/services/selectBox-directive.js']
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/proxyAccessHome/gssLandingPage-directive.js']
        resource url: [plugin: 'banner-general-proxy', file: 'proxyApp/proxyPersonalInfo/proxyPersonalInfo-directive.js']
    }

    'proxyAppLTR' {
        dependsOn "bannerWebLTR, proxyApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsLTR"
        // CSS
        resource url: [plugin: 'banner-general-proxy', file: 'css/proxy.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box.css'], attrs: [media: 'screen, projection']
    }

    'proxyAppRTL' {
        dependsOn "bannerWebRTL, proxyApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsRTL"
        // CSS
        resource url: [plugin: 'banner-general-proxy', file: 'css/proxy-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box-rtl.css'], attrs: [media: 'screen, projection']
    }

}
