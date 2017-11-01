/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import net.hedtech.banner.configuration.ApplicationConfigurationUtils as ConfigFinder
import grails.plugin.springsecurity.SecurityConfigType

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

grails.config.locations = [] // leave this initialized to an empty list, and add your locations in the map below.

def locationAdder = ConfigFinder.&addLocation.curry( grails.config.locations )

[BANNER_APP_CONFIG           : "banner_configuration.groovy",
 BANNER_GENERAL_SSB_CONFIG   : "${appName}_configuration.groovy",
 WEB_APP_EXTENSIBILITY_CONFIG: "WebAppExtensibilityConfig.class"
].each {envName, defaultFileName -> locationAdder( envName, defaultFileName )}

grails.config.locations.each {
    println "configuration: " + it
}

// ******************************************************************************
//
//                       +++ BUILD NUMBER SEQUENCE UUID +++
//
// ******************************************************************************
//
// A UUID corresponding to this project, which is used by the build number generator.
// Since the build number generator web service provides build number sequences to
// multiple projects, and each project uses a unique UUID to identify which number
// sequence it is using.
//
// This number should NOT be changed.
// FYI: When a new UUID is needed (e.g., for a new project), use this URI:
//      http://maldevl2.sungardhe.com:8080/BuildNumberServer/newUUID
//
// DO NOT EDIT THIS UUID UNLESS YOU ARE AUTHORIZED TO DO SO AND KNOW WHAT YOU ARE DOING
//
build.number.uuid = "f23ea34b-6469-4aa9-9778-e7efbba5de7b" // specific UUID for //TODO Need to regenerate
build.number.base.url = "http://m039198.ellucian.com:8080/BuildNumberServer/buildNumber?method=getNextBuildNumber&uuid="

grails.project.groupId = "net.hedtech" // used when deploying to a maven repo
grails.databinding.useSpringBinder = true
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
        html         : ['text/html', 'application/xhtml+xml'],
        xml          : ['text/xml', 'application/xml', 'application/vnd.sungardhe.student.v0.01+xml'],
        text         : 'text/plain',
        js           : 'text/javascript',
        rss          : 'application/rss+xml',
        atom         : 'application/atom+xml',
        css          : 'text/css',
        csv          : 'text/csv',
        all          : '*/*',
        json         : ['application/json', 'text/json'],
        form         : 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data',
        jpg          : 'image/jpeg',
        png          : 'image/png',
        gif          : 'image/gif',
        bmp          : 'image/bmp',
]

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.plugin.springsecurity.logout.afterLogoutUrl = "/"
grails.converters.domain.include.version = true
//grails.converters.json.date = "default"

grails.converters.json.pretty.print = true
grails.converters.json.default.deep = true

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true

grails.resources.mappers.yuicssminify.includes = ['**/*.css']
grails.resources.mappers.yuijsminify.includes = ['**/*.js']
grails.resources.mappers.yuicssminify.excludes = ['**/*.min.css']
grails.resources.mappers.yuijsminify.excludes = ['**/*.min.js']

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.resources.debug = true;
    }
}

environments {
    test {
        ssbEnabled = true
        ssbOracleUsersProxied = true
        grails.plugin.springsecurity.interceptUrlMap = [
                '/': ['IS_AUTHENTICATED_ANONYMOUSLY']]
    }
    development {
        ssbEnabled = true
        ssbOracleUsersProxied = true
    }
    production {

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

// ******************************************************************************
//
//                       +++ FORM-CONTROLLER MAP +++
//
// ******************************************************************************
// This map relates controllers to the Banner forms that it replaces.  This map
// supports 1:1 and 1:M (where a controller supports the functionality of more than
// one Banner form.  This map is critical, as it is used by the security framework to
// set appropriate Banner security role(s) on a database connection. For example, if a
// logged in user navigates to the 'medicalInformation' controller, when a database
// connection is attained and the user has the necessary role, the role is enabled
// for that user and Banner object.
formControllerMap = [
        'selfservicemenu'           : ['SELFSERVICE', 'GUAGMNU'],
        'survey'                    : ['SELFSERVICE'],
        'uploadproperties'          : ['SELFSERVICE'],
        'useragreement'             : ['SELFSERVICE'],
        'securityqa'                : ['SELFSERVICE'],
        'general'                   : ['SELFSERVICE'],
        'theme'                     : ['SELFSERVICE'],
        'themeeditor'               : ['SELFSERVICE'],
        'directdeposit'             : ['SELFSERVICE-STUDENT', 'SELFSERVICE-EMPLOYEE'],
        'personalinformation'       : ['SELFSERVICE'],
        'updateaccount'             : ['SELFSERVICE-STUDENT', 'SELFSERVICE-EMPLOYEE'],
        'accountlisting'            : ['SELFSERVICE-STUDENT', 'SELFSERVICE-EMPLOYEE'],
        'directdepositconfiguration': ['SELFSERVICE-STUDENT', 'SELFSERVICE-EMPLOYEE'],
        'personalinformationdetails': ['SELFSERVICE'],
        'personalinformationpicture': ['SELFSERVICE'],
        'jobsub-pending-print'      : ['API-JOBSUB-PRINT'],
        'personalinformationqa'     : ['SELFSERVICE'],
        //AIP//
        'aip'                       : ['SELFSERVICE'],
        'aipadmin'                  : ['SELFSERVICE-ACTIONITEMADMIN'],
        'aipactionitemposting'      : ['SELFSERVICE-ACTIONITEMADMIN'],
        'aippagebuilder'            : ['SELFSERVICE'],
        'bcm'                       : ['SELFSERVICE-ACTIONITEMADMIN',
                                       'SELFSERVICE-COMMUNICATIONUSER',
                                       'SELFSERVICE-COMMUNICATIONCONTENTADMIN',
                                       'SELFSERVICE-COMMUNICATIONADMIN' ],
        'about'                     : ['GUAGMNU'],
        'restfulapi'                : ['SELFSERVICE', 'GPBADMN'],
        'keepalive'                 : ['SELFSERVICE'],
        'dateconverter'             : ['SELFSERVICE', 'GUAGMNU'],
        'menu'                      : ['SELFSERVICE', 'GUAGMNU'],


        //PAGEBUILDER///////
        'virtualdomaincomposer'     : ['GPBADMN'],
        'cssmanager'                : ['GPBADMN'],
        'visualpagemodelcomposer'   : ['GPBADMN'],
        'cssrender'                 : ['SELFSERVICE', 'GUAGMNU'],
        'custompage'                : ['SELFSERVICE', 'GPBADMN'],
        'uploadproperties'          : ['SELFSERVICE']

]


grails.plugin.springsecurity.useRequestMapDomainClass = false
//grails.plugin.springsecurity.rejectIfNoRule = true

grails.plugin.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap

grails.plugin.springsecurity.filterChain.chainMap = [
        '/api/**': 'authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor',
        '/**'    : 'securityContextPersistenceFilter,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
]

// ******************************************************************************
//
//                       +++ INTERCEPT-URL MAP +++
//
// ******************************************************************************
pageBuilder.adminRoles = 'ROLE_GPBADMN_BAN_DEFAULT_PAGEBUILDER_M'

grails.plugin.springsecurity.interceptUrlMap = [
        '/'                                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/directDepositApp/**'               : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/personalInformationApp/**'         : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/resetPassword/**'                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/login/**'                          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/index**'                           : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/logout/**'                         : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/logout/**'                     : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/menu'                          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/js/**'                             : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/css/**'                            : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/images/**'                         : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/fonts/**'                          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/plugins/**'                        : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/errors/**'                         : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/help/**'                           : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/i18n/**'                           : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/selfServiceMenu/**'                : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/selfServiceMenu/**'            : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/menu**'                        : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/about/**'                      : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/generalSsbApp/**'                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/keepAlive/data**'              : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/dateConverter/**'                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/ssb/dateConverter/**'              : ['IS_AUTHENTICATED_ANONYMOUSLY'],
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
        // '/**': [ 'ROLE_DETERMINED_DYNAMICALLY' ]
        //'/**': [ 'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M' ]

        //AIP
        '/ssb/aip/admin/**'                  : ['ROLE_SELFSERVICE-ACTIONITEMADMIN_BAN_DEFAULT_M'],
        '/ssb/aipAdmin/**'                   : ['ROLE_SELFSERVICE-ACTIONITEMADMIN_BAN_DEFAULT_M'],
        '/ssb/BCM/**'                        : ['ROLE_SELFSERVICE-ACTIONITEMADMIN_BAN_DEFAULT_M'],
        '/ssb/aipActionItemPosting/**'       : ['ROLE_SELFSERVICE-ACTIONITEMADMIN_BAN_DEFAULT_M'],
        '/ssb/aip/**'                        : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/aipPageBuilder/**'             : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],

        //Page Builder
        '/internalPb/virtualDomains.*/**'    : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/adminPb/virtualDomains.*/**'       : [pageBuilder.adminRoles],
        '/internalPb/pages/**'               : [pageBuilder.adminRoles],
        '/internalPb/csses/**'               : [pageBuilder.adminRoles],
        '/internalPb/virtualdomains/**'      : [pageBuilder.adminRoles],
        '/internalPb/pagesecurity/**'        : [pageBuilder.adminRoles],
        '/internalPb/pageexports/**'         : [pageBuilder.adminRoles],
        '/internalPb/virtualdomainexports/**': [pageBuilder.adminRoles],
        '/internalPb/cssexports/**'          : [pageBuilder.adminRoles],
        '/internalPb/admintasks/**'          : [pageBuilder.adminRoles],
        '/virtualDomainComposer/**'          : [pageBuilder.adminRoles],
        '/visualPageModelComposer/**'        : [pageBuilder.adminRoles],
        '/cssManager/**'                     : [pageBuilder.adminRoles],
        '/admin/i18n/**'                     : [pageBuilder.adminRoles],
        '/cssRender/**'                      : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        //Restict access to newly created pages, it will be overriden once role is assigned
        '/customPage/page/**'                : ['IS_AUTHENTICATED_FULLY'],
        //Page Builder master template included to allow for users to pass in without needing role applied in requestmap table in extz app.
        '/customPage/page/AIPMasterTemplateSystemRequired/**'   : ['IS_AUTHENTICATED_FULLY'],
        //For now use a page builder dummy page for cas aut
        '/customPage/page/pbadm.ssoauth/**'  : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M', pageBuilder.adminRoles],
        //Theming specific
        '/theme/**'                          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/themeEditor/**'                    : ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M'],
        '/uploadProperties/**'               : ['ROLE_SELFSERVICE-WTAILORADMIN_BAN_DEFAULT_M'],

        '/api/**'                            : ['ROLE_DETERMINED_DYNAMICALLY'],
        '/qapi/**'                           : ['ROLE_DETERMINED_DYNAMICALLY'],
        '/api/about'                         : ['IS_AUTHENTICATED_FULLY'],
        '/api/healthcheck'                   : ['IS_AUTHENTICATED_FULLY'],
        '/ssb/securityQA/**'                 : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/survey/**'                     : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/userAgreement/**'              : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/general/**'                    : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/directDeposit/**'              : ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M'],
        '/ssb/UpdateAccount/**'              : ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M'],
        '/ssb/accountListing/**'             : ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M'],
        '/ssb/DirectDepositConfiguration/**' : ['ROLE_SELFSERVICE-EMPLOYEE_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M'],
        '/ssb/personalInformation/**'        : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/PersonalInformationDetails/**' : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/PersonalInformationPicture/**' : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M'],
        '/ssb/PersonalInformationQA/**'      : ['ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M']
]

// CodeNarc rulesets
codenarc.ruleSetFiles = "rulesets/banner.groovy"
codenarc.reportName = "target/CodeNarcReport.html"
codenarc.propertiesFile = "grails-app/conf/codenarc.properties"
codenarc.extraIncludeDirs = ["grails-app/composers"]

//grails.validateable.packages=['net.hedtech.banner.student.registration']

// placeholder for real configuration
// base.dir is probably not defined for .war file deployments
//banner.picturesPath=System.getProperty('base.dir') + '/test/images'

markdown = [
        removeHtml: true
]

grails.resources.adhoc.excludes = ['/**/*-custom.css']

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
// ******************************************************************************
// DB Connection Caching Configuration
// ******************************************************************************
// Note: The BannerDS will cache database connections for administrative users,
// however for RESTful APIs we do not want this behavior (even when
// authenticated as an 'administrative' user). RESTful APIs should be stateless.
//
// IMPORTANT:
// When exposing RESTful endpoints, exclude database caching for those URLs.
// Also, if using a prefix other than 'api' and 'qapi' you will need to ensure
// the spring security filter chain is configured to avoid creating a session.
//
avoidSessionsFor = ['api', 'qapi']

// ******************************************************************************
//                           RESTful API MEP Support
// ******************************************************************************
// Note: The BannerDS will use 'setSsbMep' even when getting an
//       administrative connection if the web request is an 'api'
//       request. Specify the 'api' prefixes using apiUrlPrefixes.
//
apiUrlPrefixes = ['api', 'qapi', 'rest', 'ui']

// ******************************************************************************
//                  RESTful API Authentication Entry Configuration
// ******************************************************************************
// Note: Specifies whether a 'restApiAuthenticationEntryPoint' bean should be used.
//       Note the 'restApiAuthenticationEntryPoint' bean *must* be registered
//       within your resources.groovy if this configuration is set to true.
//
useRestApiAuthenticationEntryPoint = true

// ******************************************************************************
//             RESTful API Custom Response Header Name Configuration
// ******************************************************************************
//
restfulApi.header.pageOffset = 'X-hedtech-pageOffset'
restfulApi.header.pageMaxSize = 'X-hedtech-pageMaxSize'
restfulApi.header.message = 'X-hedtech-message'
restfulApi.header.mediaType = 'X-Media-Type'
restfulApi.header.totalCount = 'X-Total-Count'
restfulApi.header.contentRestricted = 'X-Content-Restricted'

// ******************************************************************************
//             RESTful API 'Paging' Query Parameter Name Configuration
// ******************************************************************************
//
restfulApi.page.max = 'limit'
restfulApi.page.offset = 'offset'

// Force all marshallers to remove null fields and empty collections
restfulApi.marshallers.removeNullFields = true
restfulApi.marshallers.removeEmptyCollections = true

// ******************************************************************************
//                       RESTful API Endpoint Configuration
// ******************************************************************************
restfulApiConfig = {
    // Resources for web_app_extensibility plugin
    marshallerGroups {
        group 'json_date' marshallers {
                                          marshaller {
                                              instance = new org.codehaus.groovy.grails.web.converters.marshaller.ClosureObjectMarshaller<grails.converters.JSON>(
                                                      java.util.Date, {return it?.format( "yyyy-MM-dd" )} )
                                          }
                                      }

        group 'xml_date' marshallers {
                                         marshaller {
                                             instance = new org.codehaus.groovy.grails.web.converters.marshaller.ClosureObjectMarshaller<grails.converters.XML>(
                                                     java.util.Date, {return it?.format( "yyyy-MM-dd" )} )
                                         }
                                     }
    }
    // Begin - Query-with-POST
    resource 'query-filters' config {
                                        representation {
                                            mediaTypes = ["application/json"]
                                            jsonExtractor {}
                                        }
                                        representation {
                                            mediaTypes = ["application/xml"]
                                            xmlExtractor {}
                                        }
                                    }


    resource 'jobsub-pending-print' config {
                                               serviceName = 'jobsubOutputCompositeService'
                                               methods = ['list', 'show', 'update']
                                               representation {
                                                   mediaTypes = ["application/json"]
                                                   marshallers {
                                                       marshallerGroup 'json_date'             //for date related fields
                                                       jsonBeanMarshaller {
                                                           supports net.hedtech.banner.general.jobsub.JobsubExternalPrinter
                                                           includesFields {
                                                               field 'id'
                                                               field 'version'
                                                               field 'job'
                                                               field 'oneUpNo'
                                                               field 'fileName'
                                                               field 'printer'
                                                               field 'printForm'
                                                               field 'printDate'
                                                               field 'creatorId'
                                                               field 'printerCommand'
                                                               field 'mime'
                                                           }
                                                       }
                                                       jsonBeanMarshaller {
                                                           supports net.hedtech.banner.general.jobsub.JobsubSavedOutput
                                                           includesFields {
                                                               field 'id'
                                                               field 'version'
                                                               field 'job'
                                                               field 'fileName'
                                                               field 'printer'
                                                               field 'printForm'
                                                               field 'printDate'
                                                               field 'jobsubOutput'
                                                           }
                                                       }
                                                   }
                                                   jsonExtractor {
                                                       property 'job' name 'job'
                                                       property 'id' name 'id'
                                                       property 'printer' name 'printer'
                                                       property 'jobsubOutput' name 'jobsubOutput'
                                                   }
                                               }
                                               representation {
                                                   mediaTypes = ["application/octet-stream"]
                                                   marshallerFramework = 'jobsubOutputMarshaller'
                                               }
                                           }

    // Pagebuilder resources


    anyResource {
        serviceName = 'virtualDomainResourceService'
        representation {
            mediaTypes = ["application/json"]
            marshallers {
                marshaller {
                    instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                    priority = 100
                }
            }
            extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
        }
    }

    resource 'pagesecurity' config {
                                       serviceName = 'pageSecurityService'
                                       representation {
                                           mediaTypes = ["application/json"]
                                           marshallers {
                                               marshaller {
                                                   instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                   priority = 100
                                               }
                                           }
                                           extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                       }
                                   }

    resource 'pages' config {
                                representation {
                                    mediaTypes = ["application/json"]
                                    marshallers {
                                        jsonDomainMarshaller {
                                            priority = 101
                                        }
                                        jsonBeanMarshaller {
                                            priority = 100
                                        }
                                    }
                                    jsonExtractor {}
                                }
                            }

    resource 'csses' config {
                                representation {
                                    mediaTypes = ["application/json"]
                                    marshallers {
                                        marshaller {
                                            instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                            priority = 100
                                        }
                                    }
                                    extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                }
                            }

    resource 'virtualdomains' config {
                                         serviceName = 'virtualDomainService'
                                         representation {
                                             mediaTypes = ["application/json"]
                                             marshallers {
                                                 marshaller {
                                                     instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                     priority = 100
                                                 }
                                             }
                                             extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                         }
                                     }

    resource 'pageexports' config {
                                      serviceName = 'pageExportService'
                                      representation {
                                          mediaTypes = ["application/json"]
                                          marshallers {
                                              marshaller {
                                                  instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                  priority = 100
                                              }
                                          }
                                          extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                      }
                                  }

    resource 'virtualdomainexports' config {
                                               serviceName = 'virtualDomainExportService'
                                               representation {
                                                   mediaTypes = ["application/json"]
                                                   marshallers {
                                                       marshaller {
                                                           instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                           priority = 100
                                                       }
                                                   }
                                                   extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                               }
                                           }
    resource 'cssexports' config {
                                     serviceName = 'cssExportService'
                                     representation {
                                         mediaTypes = ["application/json"]
                                         marshallers {
                                             marshaller {
                                                 instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                 priority = 100
                                             }
                                         }
                                         extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                     }
                                 }

    resource 'admintasks' config {
                                     serviceName = 'adminTaskService'
                                     representation {
                                         mediaTypes = ["application/json"]
                                         marshallers {
                                             marshaller {
                                                 instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                 priority = 100
                                             }
                                         }
                                         extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                     }
                                 }
    // This pseudo resource is used when issuing a query using a POST. Such a POST is made
    // against the actual resource being queried, but using a different URL prefix (e.g., qapi)
    // so the request is routed to the 'list' method (versus the normal 'create' method).
    resource 'query-filters' config {
                                        // TODO: Add support for 'application/x-www-form-urlencoded'
                                        representation {
                                            mediaTypes = ["application/json"]
                                            jsonExtractor {}
                                        }
                                    }

    // 2 demo resources
    resource 'todos' config {
                                representation {
                                    mediaTypes = ["application/json"]
                                    marshallers {
                                        marshaller {
                                            instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                            priority = 100
                                        }
                                    }
                                    extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                }
                            }

    resource 'projects' config {
                                   representation {
                                       mediaTypes = ["application/json"]
                                       marshallers {
                                           marshaller {
                                               instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                               priority = 100
                                           }
                                       }
                                       extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                   }
                               }
    //END of pagebuilder configuration
    resource 'extensions' config {
                                     serviceName = 'webAppExtensibilityExtensionService'
                                     // In some cases the service name has to be prepended with the plugin name
                                     representation {
                                         mediaTypes = ["application/json"]
                                         marshallers {
                                             marshaller {
                                                 instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                 priority = 100
                                             }
                                         }
                                         extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                     }
                                 }
    resource 'resources' config {
                                    serviceName = 'webAppExtensibilityResourceService'
                                    // In some cases the service name has to be prepended with the plugin name
                                    representation {
                                        mediaTypes = ["application/json"]
                                        marshallers {
                                            marshaller {
                                                instance = new net.hedtech.restfulapi.marshallers.json.BasicDomainClassMarshaller( app: grailsApplication )
                                                priority = 100
                                            }
                                        }
                                        extractor = new net.hedtech.restfulapi.extractors.json.DefaultJSONExtractor()
                                    }
                                }
    // End Resources for web_app_extensibility plugin
}
grails.plugin.springsecurity.cas.active = false
grails.plugin.springsecurity.saml.active = false
