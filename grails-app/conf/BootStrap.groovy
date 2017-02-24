/*******************************************************************************
 Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.converters.JSON
import grails.util.Environment
import net.hedtech.banner.converters.json.JSONBeanMarshaller
import net.hedtech.banner.converters.json.JSONDomainMarshaller
import net.hedtech.banner.i18n.LocalizeUtil
import org.apache.commons.logging.LogFactory
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.codehaus.groovy.grails.web.converters.configuration.ConverterConfiguration
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.codehaus.groovy.grails.web.converters.configuration.DefaultConverterConfiguration

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * */
class BootStrap {

    def log = Logger.getLogger(this.getClass())
    def dateConverterService

    def localizer = { mapToLocalize ->
        new ValidationTagLib().message(mapToLocalize)
    }

    def grailsApplication
    def resourceService

    def init = { servletContext ->
        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)

        /**
         * Using dataSource to set properties is not allowed after grails 1.3. dataSourceUnproxied should be used instead
         * Disabling it for now to avoid compatibility issue.
         */
        // Configure the dataSource to ensure connections are tested prior to use
        /*        ctx.dataSourceUnproxied.with {
            setMinEvictableIdleTimeMillis( 1000 * 60 * 30 )
            setTimeBetweenEvictionRunsMillis( 1000 * 60 * 30 )
            setNumTestsPerEvictionRun( 3 )
            setTestOnBorrow( true )
            setTestWhileIdle( false )
            setTestOnReturn( false )
            setValidationQuery( "select 1 from dual" )
        }*/

        if (Environment.current != Environment.TEST) {
            // println("Reading format from ${servletContext.getRealPath("/xml/application.navigation.conf.xml" )}")
            // NavigationConfigReader.readConfigFile( servletContext.getRealPath("/xml/application.navigation.conf.xml" ) )
        }


        grailsApplication.controllerClasses.each {
            log.info "adding log property to controller: $it"
            // Note: weblogic throws an error if we try to inject the method if it is already present
            if (!it.metaClass.methods.find { m -> m.name.matches("getLog") }) {
                def name = it.name // needed as this 'it' is not visible within the below closure...
                try {
                    it.metaClass.getLog = { LogFactory.getLog("$name") }
                }
                catch (e) { } // rare case where we'll bury it...
            }
        }

        grailsApplication.allClasses.each {
            if (it.name?.contains("plugin.resource")) {
                log.info "adding log property to plugin.resource: $it"

                // Note: weblogic throws an error if we try to inject the method if it is already present
                if (!it.metaClass.methods.find { m -> m.name.matches("getLog") }) {
                    def name = it.name // needed as this 'it' is not visible within the below closure...
                    try {
                        it.metaClass.getLog = { LogFactory.getLog("$name") }
                    }
                    catch (e) { } // rare case where we'll bury it...
                }
            }
        }

        // Register the JSON Marshallers for format conversion and XSS protection
        registerJSONMarshallers()

        resourceService.reloadAll()


        List.metaClass.sortAndPaginate = { max, offset = 0, sortColumn, sortDirection = "asc" ->

            List delegateList = new ArrayList(delegate);
            def sorted = delegateList.sort { a, b ->
                a[sortColumn].compareToIgnoreCase b[sortColumn]
            }

            if (sortDirection == "desc")
                sorted = sorted.reverse()

            sorted.subList( offset, Math.min( offset + max, delegate.size() ) )
        }

}


def destroy = {
    // no-op
}


private def registerJSONMarshallers() {
    Closure marshaller = { it ->
        dateConverterService.parseGregorianToDefaultCalendar(LocalizeUtil.formatDate(it))
    }

    JSON.registerObjectMarshaller(Date, marshaller)

    ConverterConfiguration cfg = ConvertersConfigurationHolder.getNamedConverterConfiguration ("deep", JSON.class);
    ((DefaultConverterConfiguration) cfg).registerObjectMarshaller(Date, marshaller);


    def localizeMap = [
            'attendanceHour': LocalizeUtil.formatNumber,
    ]

    JSON.registerObjectMarshaller(new JSONBeanMarshaller( localizeMap ), 1) // for decorators and maps
    JSON.registerObjectMarshaller(new JSONDomainMarshaller( localizeMap, true), 2) // for domain objects
}


}
