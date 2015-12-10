/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Environment
import groovy.xml.StreamingMarkupBuilder
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

// Ensures logging is visible when using 'grails run-app', by adding a valve to the embedded tomcat.
// Note we need to load dynamically, as _Events is loaded when running scripts where this class may not always available.
def appName = grails.util.Metadata.current.'app.name'
eventConfigureTomcat = { tomcat ->
    Class AccessLogValve = Class.forName('org.apache.catalina.valves.AccessLogValve', true, this.getClass().getClassLoader())
    def valve = AccessLogValve.newInstance(directory: basedir, prefix: "${appName}-catalina.", pattern: 'common')
    tomcat.host.addValve valve
}

def preparePlugin = { name, prepare ->
    def plugins = GrailsPluginUtils.getSupportedPluginInfos()

    if (plugins) {
        def pluginInfo = plugins.find { info -> info.name == name }

        if (pluginInfo) {
            prepare(pluginInfo.name, pluginInfo.version, "${basedir}/plugins/${pluginInfo.name.tr('-', '_')}.git")
        }
        else {
            throw new RuntimeException("Plugin '$name' not found as a supported plugin.")
        }
    }
    else {
        throw new RuntimeException("Unable to obtain the list of supported plugins.")
    }
}

//Introduced eventCompileStart in the _Events.groovy at the application-side to delete the unwanted generated files - ZkUrlMappings.groovy and
// SeleniumConfig.groovy. It also uses to copy messages.properties files.
/*eventCompileStart = {kind ->
    println ">>>>>> Copying message.properties file"
    ant.copy(todir: "$grailsSettings.baseDir/web-app/WEB-INF/grails-app/i18n/") {
        fileset(dir: "$grailsSettings.baseDir/grails-app/i18n/")
    }
    // Delete the ZKUrlMappings.groovy file and SeleniumConfig.groovy file creates under plugin
    // before start compiling. Following script checks the file and deletes if it exists.
    def pluginInfos = GrailsPluginUtils.getPluginDirectories()
    String fileName = ""
    pluginInfos.each {
        fileName = "${it}\\grails-app\\conf\\ZkUrlMappings.groovy"
        deleteFile(fileName)
        fileName = "${it}\\grails-app\\conf\\SeleniumConfig.groovy"
        deleteFile(fileName)
    }
}*/

private void deleteFile(String fileName) {
    if (new File("${fileName} ").exists()) {
        fileName = StringUtils.replace(fileName, "", " / ")
        ant.delete(file: "${fileName}")
    }
}

// Remove the JDBC jar before the war is bundled
eventCreateWarStart = { warName, stagingDir ->
    // When deploying a war it is important to exclude the Oracle database drivers.  Not doing so will
    // result in the all-too-familiar exception:
    // "Cannot cast object 'oracle.jdbc.driver.T4CConnection@6469adc7'... to class 'oracle.jdbc.OracleConnection'
    //
    // We'll move this to 'target/' so that our 'package-release' target (from banner_packaging) may copy it
    // into the product home, as a convenience to the client.
    //
    ant.move( file:"${stagingDir}/WEB-INF/lib/ojdbc6-11.2.0.1.0.jar", toFile:"$basedir/target/ojdbc6.jar" )
    ant.move( file:"${stagingDir}/WEB-INF/lib/xdb6-11.2.0.4.jar", toFile:"$basedir/target/xdb6.jar" )

    preparePlugin("banner-ui-ss") { name, version, pluginDirectory ->
        println "Copying CSS, image, and JavaScript files from banner-ui-ss plugin"

        Ant.copy(todir: "${stagingDir}/css") {
            fileset(dir: "${pluginDirectory}/web-app/css")
        }

        Ant.copy(todir: "${stagingDir}/images") {
            fileset(dir: "${pluginDirectory}/web-app/images")
        }

        Ant.copy(todir: "${stagingDir}/js") {
            fileset(dir: "${pluginDirectory}/web-app/js")
        }

        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }
    
    preparePlugin("banner-general-common-ui-ss") { name, version, pluginDirectory ->
        println "Copying CSS, image, and JavaScript files from banner-ui-ss plugin"

        Ant.copy(todir: "${stagingDir}/css") {
            fileset(dir: "${pluginDirectory}/web-app/css")
        }

        Ant.copy(todir: "${stagingDir}/images") {
            fileset(dir: "${pluginDirectory}/web-app/images")
        }

        Ant.copy(todir: "${stagingDir}/js") {
            fileset(dir: "${pluginDirectory}/web-app/js")
        }

        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

    preparePlugin("sghe-aurora") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

    preparePlugin("i18n-core") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }


    preparePlugin("banner-core") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

/*    preparePlugin("banner-payroll-common") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }
*/
    preparePlugin("banner-general-common") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

    preparePlugin("banner-general-validation-common") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

    preparePlugin("banner-general-person") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
        println "Copying no_photo_available.png file from banner-general-person"

        ant.copy (file: "${pluginDirectory}/web-app/images/no_photo_available.png",
                todir: "${stagingDir}/images")
    }

    preparePlugin("banner-ui-ss") { name, version, pluginDirectory ->
        Ant.copy(todir: "${stagingDir}/WEB-INF/plugins/$name-$version/grails-app/i18n") {
            fileset(dir: "${pluginDirectory}/grails-app/i18n")
        }
    }

    // Next, we need to remove some additional jars
    // (that are sometimes added when the createWar occurs as a dependency to other scripts)
    ant.delete {
        fileset(dir: "${stagingDir}/WEB-INF/lib", includes: "*tomcat-*.jar", excludes: "tomcat-jdbc-*.jar, tomcat-juli-*.jar")
        fileset(dir: "${stagingDir}/WEB-INF/lib", includes: "catalina-ant.jar")
        fileset(dir: "${stagingDir}/WEB-INF/lib", includes: "jasper-jdt.jar")
        fileset(dir: "${stagingDir}/WEB-INF/lib", includes: "com.springsource.org.apache.xml.security-1.0.5.D2.jar")
    }

    ant.delete(dir: "${stagingDir}/WEB-INF/classes/functionaltestplugin")
    ant.delete(dir: "${stagingDir}/plugins/functional-test-2.0.0")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTest.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure1.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure2.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure3.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure4.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure5.class")
    ant.delete(file: "${stagingDir}/WEB-INF/classes/FunctionalTestGrailsPlugin\$_closure6.class")
    ant.delete(dir: "${stagingDir}/WEB-INF/classes/net/hedtech/banner/testing")
    ant.delete(dir: "${stagingDir}/selenium")

    ant.delete(dir: "${stagingDir}/WEB-INF/plugins/selenium-rc*")
    ant.delete(dir: "${stagingDir}/WEB-INF/plugins/tomcat*")

    def grailsXmlFile = new File("${stagingDir}/WEB-INF/grails.xml")
    if (grailsXmlFile.exists()) {
        String textToRemove = "<plugin>FunctionalTestGrailsPlugin</plugin>"
        ant.replace(file: grailsXmlFile, token: textToRemove, value: "")
    }
}

/**
 * A workaround for a grails JIRA which has not been fixed
 * GRAILS-5661: http://jira.codehaus.org/browse/GRAILS-5661
 * This unfortunately generates a non-JEE spec web descriptor
 * This also adds the resource reference to the JNDI name of the datasource.
 * The datasource that should be configured either in weblogic or tomacat
 * should be named jdbc/bannerDataSource
 */
if (Environment.current == Environment.PRODUCTION) {
    eventWebXmlEnd = { String webXml ->
        def root = new XmlSlurper().parse(webXmlFile)

        def errorPage = root.'error-page'
        // If there are more than 1 error page entries - delete all but the first
        errorPage.eachWithIndex { item, i ->
            if (i != 0) item.replaceNode {}
        }

        root.appendNode {
            'resource-ref' {
                'description'('BannerDS Datasource')
                'res-ref-name'('jdbc/bannerDataSource')
                'res-type'('javax.sql.DataSource')
                'res-auth'('Container')
            }
        }

        root.appendNode {
            'resource-ref' {
                'description'('BannerSelfService Datasource')
                'res-ref-name'('jdbc/bannerSsbDataSource')
                'res-type'('javax.sql.DataSource')
                'res-auth'('Container')
            }
        }

        webXmlFile.text = new StreamingMarkupBuilder().bind {
            mkp.declareNamespace("": "http://java.sun.com/xml/ns/javaee")
            mkp.yield(root)
        }
    }
}

