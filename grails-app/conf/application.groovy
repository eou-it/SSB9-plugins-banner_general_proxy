
/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

// ******************************************************************************
//
//                       +++ EXTERNALIZED CONFIGURATION +++
//
// ******************************************************************************
//
// Config locations should be added to the map used below. They will be loaded based upon this search order:
// 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
// 2. Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
// 3. Load the configuration file if its location was specified as a system environment variable
//
// Map [ environment variable or -D command line argument name : file path ]
grails.config.locations = [] // leave this initialized to an empty list, and add your locations
// in the APPLICATION CONFIGURATION section below.

// TODO:grails_332_change, need to add external config location array revisit this - For Integration Test cases


grails.databinding.useSpringBinder=true

grails.project.groupId = "net.hedtech" // used when deploying to a maven repo

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
                     xml: ['text/xml', 'application/xml'],
                     text: 'text/plain',
                     js: 'text/javascript',
                     rss: 'application/rss+xml',
                     atom: 'application/atom+xml',
                     css: 'text/css',
                     csv: 'text/csv',
                     all: '*/*',
                     json: ['application/json', 'text/json'],
                     form: 'application/x-www-form-urlencoded',
                     multipartForm: 'multipart/form-data'
]

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** Charlie note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

grails.converters.domain.include.version = true
grails.converters.json.date = "javascript"
grails.converters.json.pretty.print = true

banner {
    sso {
        authenticationProvider = 'default'
        authenticationAssertionAttribute = 'udcId'
    }
}

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
//        grails.serverURL = "http://localhost:8080/${appName}"
    }
    development {
//        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        ssbEnabled = true
        ssbOracleUsersProxied = true
//        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

// ******************************************************************************
//
//                       +++ DATA ORIGIN CONFIGURATION +++
//
// ******************************************************************************
// This field is a Banner standard, along with 'lastModifiedBy' and lastModified.
// These properties are populated automatically before an entity is inserted or updated
// within the database. The lastModifiedBy uses the username of the logged in user,
// the lastModified uses the current timestamp, and the dataOrigin uses the value
// specified here:
dataOrigin = "Banner"

grails.plugin.springsecurity.useRequestMapDomainClass = false

grails.plugin.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap


// ******************************************************************************
//
//                       +++ INTERCEPT-URL MAP +++
//
// ******************************************************************************

grails.plugin.springsecurity.interceptUrlMap = [
        '/': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/menu/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/login/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/mainPage**': ['ROLE_SCACRSE_BAN_DEFAULT_M'],
        '/menu/**': ['ROLE_SCACRSE_BAN_DEFAULT_M'],
        '/index**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/logout/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/js/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/css/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/images/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/plugins/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/errors/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],

        // ALL URIs specified with the BannerAccessDecisionVoter.ROLE_DETERMINED_DYNAMICALLY
        // 'role' (it's not a real role) will result in authorization being determined based
        // upon a user's role assignments to the corresponding form (see 'formControllerMap' above).
        // Note: This 'dynamic form-based authorization' is performed by the BannerAccessDecisionVoter
        // registered as the 'roleVoter' within Spring Security.
        //
        // Only '/name_used_in_formControllerMap/' and '/api/name_used_in_formControllerMap/'
        // URL formats are supported.  That is, the name_used_in_formControllerMap must be first, or
        // immediately after 'api' -- but it cannot be otherwise nested. URIs may be protected
        // by explicitly specifying true roles instead -- as long as ROLE_DETERMINED_DYNAMICALLY
        // is NOT specified.
        //
        '/**': [ 'ROLE_DETERMINED_DYNAMICALLY' ]
]


// configuration for plugin testing - will not be included in the plugin zip

// Representations officially supported within Banner. Custom representations should not be added to this map,
// but should instead be added to a 'CustomRepresentationConfig.groovy' file, within a 'customRepresentationHandlerMap'
// that follows the same structure as the map below.  Please see the configuration location information earlier in
// this file regarding options for externalizing this 'CustomRepresentationConfig' configuration.
        //
// Following are the 'built-in' representations supported by Ellucian. Note that representation support may be in the
// form of custom handler classes, or may be implemented in-line within the map below via closures.  See the
// banner-core plugin test-banner-core test application's Config.groovy for an example of in-lining representation support.
//
bannerRepresentationHandlerMap = [
        "application/vnd.sungardhe.banner.general.v0.01+xml": ["College": "net.hedtech.banner.general.system.CollegeRepresentationHandler_v0_01",
                // next Model supported goes here
        ],
        // The next MIME type(s) should be added here
        // "application/vnd.sungardhe.xxxxxxxxxxx.v0.00+xml": [ "College": "net.hedtech.banner.general.system.CollegeRepresentationHandler_v0_01",
        //                                                      // next Model supported goes here
        //                                                    ],
]



// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */


// Note: Most of the dataSource configuration resides in resources.groovy and in the
// installation-specific configuration file (see Config.groovy for the include).

dataSource {
    //configClass = GrailsAnnotationConfiguration.class
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    //loggingSql = false

}


hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory'
    show_sql = false
    packagesToScan="net.hedtech.**.*"
    flush.mode = AUTO
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    config.location = [
            "classpath:hibernate-banner-finance-common.cfg.xml"
    ]
}

// environment specific settings
environments {
    development {
        dataSource {
        }
    }
    test {
        dataSource {
        }
    }
    production {
        dataSource {
        }
    }
}
//Added for integration tests to run in plugin level
grails.config.locations = [
        BANNER_APP_CONFIG: "banner_configuration.groovy"
]