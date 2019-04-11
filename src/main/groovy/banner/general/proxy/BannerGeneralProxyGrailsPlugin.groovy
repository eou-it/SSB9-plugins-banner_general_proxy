/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/

package banner.general.proxy

import grails.plugins.*

/**
 * A Grails plugin which is the Banner General Personal Information sub-application in the
 * consolidated Banner XE application.
 **/
class BannerGeneralProxyGrailsPlugin extends Plugin {

    // Note: the groupId 'should' be used when deploying this plugin via the 'grails maven-deploy --repository=snapshots' command,
    // however it is not being picked up.  Consequently, a pom.xml file is added to the root directory with the correct groupId
    // and will be removed when the maven-publisher plugin correctly sets the groupId based on the following field.
    String groupId = "net.hedtech"

    // Note: Using '0.1-SNAPSHOT' (to put a timestamp on the artifact) is not used due to GRAILS-5624 see: http://jira.codehaus.org/browse/GRAILS-5624
    // Until this is resolved, Grails application's that use a SNAPSHOT plugin do not check for a newer plugin release, so that the
    // only way we'd be able to upgrade a project would be to clear the .grails and .ivy2 cache to force a fetch from our Nexus server.
    // Consequently, we'll use 'RELEASES' so that each project can explicitly identify the needed plugin version. Using RELEASES provides
    // more control on 'when' a grails app is updated to use a newer plugin version, and therefore 'could' allow delayed testing within those apps
    // independent of deploying a new plugin build to Nexus.
    //
    String version = "0.1"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"


    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Ellucian"
    def authorEmail = "actionline@ellucian.com"
    def title = "Banner General Person Proxy Plugin"
    def description = '''This plugin is BannerGeneralProxy.'''//.stripMargin()  // TODO Enable this once we adopt Groovy 1.7.3

    def documentation = "http://sungardhe.com/development/horizon/plugins/banner-general-proxy"


    Closure doWithSpring() {
        { ->
            // TODO Implement runtime spring config (optional)
        }
    }


    void doWithDynamicMethods() {
            // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
            // TODO Implement post initialization spring config (optional)
        }

    void onChange(Map<String, Object> event) {
            // TODO Implement code that is executed when any artefact that this plugin is
            // watching is modified and reloaded. The event contains: event.source,
            // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
            // TODO Implement code that is executed when the project configuration changes.
            // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
            // TODO Implement code that is executed when the application shuts down (optional)
    }

}
