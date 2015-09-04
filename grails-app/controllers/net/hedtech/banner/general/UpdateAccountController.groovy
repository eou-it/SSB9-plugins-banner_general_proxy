package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.LocalizeUtil
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.core.context.SecurityContextHolder

class UpdateAccountController {

    def directDepositAccountService
    
    def createAccount() {
        def model = [:]
        def map = request?.JSON ?: params
        map.pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        
        // default values for a new Direct Deposit account
        map.status = 'P'
        map.documentType = 'D'
        map.intlAchTransactionIndicator = 'N'
        
        log.debug("trying to create acct: "+ map.bankAccountNum)
        
        try {
            render directDepositAccountService.create(map) as JSON

        } catch (ApplicationException e) {
            model.failure = true;
            try {
                model.message = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) }).message
                render model as JSON
            } catch (ApplicationException ex) {
                log.error(ex)
                model.message = ex.message
                render model as JSON
            }
        }
    }

}
