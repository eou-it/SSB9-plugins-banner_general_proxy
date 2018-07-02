/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {
    /* Override UI Bootstrap 0.10.0 to use version 0.13.3
     * Override AngularUI Router 0.2.10 to use version 0.2.15 */
    overrides {
        'angularApp' {
            resource id: [plugin: 'banner-ui-ss', file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js'], url: [file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
            resource id: [plugin: 'banner-ui-ss', file: 'js/angular/angular-ui-router.min.js'], url: [file: 'js/angular/angular-ui-router.min.js']
        }
    }
    'angularGeneral' {
        resource url: [plugin: 'banner-ui-ss', file: 'js/angular/angular-route.min.js']

    }
    'generalSsbApp' {
        dependsOn "angularGeneral"

        defaultBundle environment == "development" ? false : "generalSsbApp"

        //Main configuration file
        resource url: [file: 'generalSsbApp/app.js']

        // Services
        resource url: [file: 'generalSsbApp/common/services/breadcrumb-service.js']
        resource url: [file: 'generalSsbApp/common/services/generalSsb-service.js']

        // Controllers
        resource url: [file: 'generalSsbApp/landingPage/gssLandingPage-controller.js']

        // Filters
        resource url: [file: 'generalSsbApp/common/filters/i18n-filter.js']

        // Directives
        resource url: [file: 'generalSsbApp/landingPage/gssLandingPage-directive.js']

    }

    'generalSsbAppLTR' {
        dependsOn "bannerWebLTR, generalSsbApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsLTR"
        // CSS
        resource url: [file: 'css/generalSsbMain.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive.css'], attrs: [media: 'screen, projection']
    }

    'generalSsbAppRTL' {
        dependsOn "bannerWebRTL, generalSsbApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsRTL"
        // CSS
        resource url: [file: 'css/generalSsbMain-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive-rtl.css'], attrs: [media: 'screen, projection']
    }

    'proxyApp' {
        dependsOn "angularGeneral"

        defaultBundle environment == "development" ? false : "proxyApp"

        //Main configuration file
        resource url: [file: 'proxyApp/app.js']

        // Services
        resource url: [file: 'proxyApp/common/services/breadcrumb-service.js']
        resource url: [file: 'proxyApp/common/services/proxyApp-service.js']

        // Controllers
        resource url: [file: 'proxyApp/proxyAccessHome/gssLandingPage-controller.js']
        resource url: [file: 'proxyApp/proxyPersonalInfo/proxyPersonalInfo-controller.js']
        resource url: [file: 'proxyApp/student/proxyViewHolds-controller.js']

        // Filters
        resource url: [file: 'proxyApp/common/filters/i18n-filter.js']

        // Directives
        resource url: [file: 'proxyApp/common/services/selectBox-directive.js']
        resource url: [file: 'proxyApp/proxyAccessHome/gssLandingPage-directive.js']
        resource url: [file: 'proxyApp/proxyPersonalInfo/proxyPersonalInfo-directive.js']

    }

    'proxyAppLTR' {
        dependsOn "bannerWebLTR, proxyApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsLTR"
        // CSS
        resource url: [file: 'css/proxy.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box.css'], attrs: [media: 'screen, projection']
    }

    'proxyAppRTL' {
        dependsOn "bannerWebRTL, proxyApp, i18n-core, glyphicons, bootstrap, auroraCommon, commonComponents, commonComponentsRTL"
        // CSS
        resource url: [file: 'css/proxy-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [file: 'css/generalSsbResponsive-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box-rtl.css'], attrs: [media: 'screen, projection']
    }

    'commonComponents' {
        resource url: [file: 'js/d3/d3.min.js']
        resource url: [file: 'js/xe-components/xe-ui-components.js']
    }
    'commonComponentsLTR' {
        dependsOn 'commonComponents'
        resource url: [file: 'css/xe-components/xe-ui-components.min.css']
    }
    'commonComponentsRTL' {
        dependsOn 'commonComponents'
        resource url: [file: 'css/xe-components/xe-ui-components-rtl.min.css']
    }

}
