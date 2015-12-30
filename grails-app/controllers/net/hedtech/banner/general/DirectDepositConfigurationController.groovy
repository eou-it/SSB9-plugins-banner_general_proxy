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
            render returnFailureMessage(e) as JSON
        }
    }

    private def returnFailureMessage(ApplicationException  e) {
        def model = [:]
        model.failure = true
        log.error(e)
        try {
            model.message = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) }).message
            return model
        } catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }

}
