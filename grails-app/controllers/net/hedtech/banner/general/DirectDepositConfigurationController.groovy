package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

class DirectDepositConfigurationController {

    def directDepositConfigurationService

    def getConfig() {
        try {
            render directDepositConfigurationService.getDirectDepositParams() as JSON
        } catch (ApplicationException e) {
            render ControllerUtility.returnFailureMessage(e) as JSON
        }
    }

}
