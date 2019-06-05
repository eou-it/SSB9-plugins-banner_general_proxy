
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
            "classpath:hibernate-banner-core.cfg.xml",
            "classpath:hibernate-banner-general-person.cfg.xml",
            "classpath:hibernate-banner-general-validation-common.cfg.xml",
            "classpath:hibernate-banner-general-common.cfg.xml",
            "classpath:hibernate-banner-general-utility.cfg.xml",
            "classpath:hibernate-banner-student-history.cfg.xml"
    ]
}


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


environments {
    test {
        ssbEnabled = true
        ssbOracleUsersProxied = true
        grails.plugin.springsecurity.interceptUrlMap = [
                '/': ['IS_AUTHENTICATED_ANONYMOUSLY'] ]
    }
    development {
        ssbEnabled = true
        ssbOracleUsersProxied = true
    }
    production {

    }

}

//Added for integration tests to run in plugin level
grails.config.locations = [
        BANNER_APP_CONFIG: "banner_configuration.groovy"
]