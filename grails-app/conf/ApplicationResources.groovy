/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

modules = {
    /* Override UI Bootstrap 0.10.0 to use UI Bootstrap version 0.13.3 */
    overrides {
        'angularApp' {
            resource id:[plugin: 'banner-ui-ss',file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js'], url: [file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
        }
    }

    'angular' {
        resource url:[file: 'js/angular/angular-route.min.js']
    }

   'generalSsbApp' {
       dependsOn "angular,glyphicons,bootstrap"

       defaultBundle environment == "development" ? false : "generalSsbApp"

       //Main configuration file
       resource url: [file: 'generalSsbApp/app.js']

       // Services
       resource url:[file: 'generalSsbApp/ddListing/ddListing-service.js']
       resource url:[file: 'generalSsbApp/common/services/breadcrumb-service.js']
       resource url:[file: 'generalSsbApp/common/services/notificationcenter-service.js']
       resource url:[file: 'generalSsbApp/common/services/directDeposit-service.js']
       resource url:[file: 'generalSsbApp/ddEditAccount/ddEditAccount-service.js']


       // Controllers
       resource url:[file: 'generalSsbApp/ddListing/ddListing-controller.js']
       resource url:[file: 'generalSsbApp/ddEditAccount/ddEditAccount-controller.js']

       // Filters
       resource url:[file: 'generalSsbApp/common/filters/i18n-filter.js']
       resource url:[file: 'generalSsbApp/common/filters/accountNumMask-filter.js']

       // Directives
       resource url:[file: 'generalSsbApp/ddListing/ddListing-directive.js']
       resource url:[file: 'generalSsbApp/ddEditAccount/ddEditAccount-directive.js']
       resource url:[file: 'generalSsbApp/common/directives/enterKey-directive.js']
       resource url:[file: 'generalSsbApp/common/directives/ddPopover-directive.js']

   }
   
   'generalSsbAppLTR' {
      dependsOn "bannerWebLTR, generalSsbApp"
      
       // CSS
       resource url:[file: 'css/main.css'],   attrs: [media: 'screen, projection']
       resource url:[file: 'css/responsive.css'],   attrs: [media: 'screen, projection']
       resource url:[file: 'css/banner-icon-font.css'],   attrs: [media: 'screen, projection']
   }
   
   'generalSsbAppRTL' {
      dependsOn "bannerWebRTL, generalSsbApp"
      
       // CSS
       resource url:[file: 'css/main-rtl.css'],   attrs: [media: 'screen, projection']
       resource url:[file: 'css/responsive-rtl.css'],   attrs: [media: 'screen, projection']
       resource url:[file: 'css/banner-icon-font-rtl.css'],   attrs: [media: 'screen, projection']
   }

}
