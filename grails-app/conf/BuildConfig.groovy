/*********************************************************************************
 Copyright 2015 Ellucian.
 *********************************************************************************/

grails.project.dependency.resolver = "ivy"
grails.project.class.dir        = "target/classes"
grails.project.lib.dir          = "lib"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

// When deploying a war it is important to exclude the Oracle database drivers.  Not doing so will
// result in the all-too-familiar exception:
// "Cannot cast object 'oracle.jdbc.driver.T4CConnection@6469adc7'... to class 'oracle.jdbc.OracleConnection'
grails.war.resources = { stagingDir ->
    delete(file: "${stagingDir}/WEB-INF/lib/ojdbc6.jar")
}

    grails.plugin.location.'i18n_core'                        = "plugins/i18n_core.git"
    grails.plugin.location.'banner-ui-ss'                     = "plugins/banner_ui_ss.git"
    grails.plugin.location.'banner-core'                      = "plugins/banner_core.git"
    grails.plugin.location.'banner-codenarc'                  = "plugins/banner_codenarc.git"
    grails.plugin.location.'spring-security-cas'              = "plugins/spring_security_cas.git"
    grails.plugin.location.'banner-general-person'            = "plugins/banner_general_person.git"
    grails.plugin.location.'banner-general-common'            = "plugins/banner_general_common.git"
    grails.plugin.location.'banner-general-utility'           = "plugins/banner_general_utility.git"
    grails.plugin.location.'banner-seeddata-catalog'          = "plugins/banner_seeddata_catalog.git"
    grails.plugin.location.'banner-general-validation-common' = "plugins/banner_general_validation_common.git"
    grails.plugin.location.'sghe-aurora'                      = "plugins/sghe_aurora.git"
    grails.plugin.location.'banner-packaging'                 = "plugins/banner_packaging.git"
    grails.plugin.location.'banner-general-common-ui-ss'      = "plugins/banner_general_common_ui_ss.git"
    grails.plugin.location.'domain-extension'                 = "plugins/domain_extension.git"


grails.project.dependency.resolution = {

    inherits "global" // inherit Grails' default dependencies
    log      "warn"   // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
        mavenRepo "http://repository.codehaus.org"
    }

    plugins {
        compile ':spring-security-core:1.2.7.3'
        compile ':resources:1.1.6'
        compile ':zipped-resources:1.0'
        compile ':cached-resources:1.0'
        compile ':yui-minify-resources:0.1.4'
        compile ':cache-headers:1.1.5'
        compile ":hibernate:3.6.10.10"
        build ":tomcat:7.0.52.1"
        test ':code-coverage:1.2.5'
        compile ":functional-test:2.0.0"
        runtime ":webxml:1.4.1"
        compile ':codenarc:0.21'
        compile ':markdown:1.0.0.RC1'
        runtime ":rendering:1.0.0"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // Note: elvyx-1.0.24_beta.jar remains in the lib/ directory of the project as it is not available in a public repo due to licensing issues.
        build 'org.antlr:antlr:3.2',
              'com.thoughtworks.xstream:xstream:1.2.1',
              'javassist:javassist:3.8.0.GA'
        runtime "javax.servlet:jstl:1.1.2"

        runtime 'org.springframework:spring-test:3.1.0.RELEASE'
    }
}

grails.war.resources = { stagingDir, args ->

    [delete(dir: "${stagingDir}/WEB-INF/classes/functionaltestplugin"),
            delete(dir: "${stagingDir}/plugins/functional-test-2.0.0"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTest.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure1.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure2.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure3.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure4.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure5.class"),
            delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure6.class"),
            delete(dir:  "${stagingDir}/WEB-INF/classes/net/hedtech/banner/testing"),
            delete(dir:  "${stagingDir}/selenium")
    ]
}

grails.war.copyToWebApp = { args ->
    fileset(dir:"web-app") {
        include(name: "hrApp/**")
        include(name: "js/**")
        include(name: "css/**")
        include(name: "images/**")
        include(name: "fonts/**")
        include(name: "WEB-INF/**")
    }
}


/* ******************************************************************************
 *                        Test Coverage Configuration                           *
 ********************************************************************************/

// NOTE: Please use test coverage analysis to help improve code quality.  Please also understand that
//       100% coverage in a class may not be sufficient, and that low coverages may also be 'acceptable'
//       in cases.  Also understand that if one looks at code coverage during integration testing, this
//       does not reflect code coverage during 'functional' testing.

coverage {
       exclusions = [ "**/CustomRepresentationConfig*",
                      "**/SeleniumConfig*",
                      "**/seeddata/**",
                      "**/ui/**",
                      "**/*RepresentationBuilder*",
                      "**/*RepresentationHandler*",
                      "**/*ParamsExtractor*"
                    ]
}

reportMessages {
       // put all keys here, that should not show as unused, even if no code reference could be found
       // note that it is sufficient to provide an appropriate prefix to match a group of keys
       exclude = ["default", "typeMismatch"]

       // put all variable names here, that are used in dynamic keys and have a defined set of values
       // e.g. if you have a call like <c:message code="show.${prod}" /> and "prod" is used in many
       // pages to distinguish between "orange" and "apple" add a map to the list below:
       //     prod: ["orange", "apple"]
       dynamicKeys = [
       ]
}
