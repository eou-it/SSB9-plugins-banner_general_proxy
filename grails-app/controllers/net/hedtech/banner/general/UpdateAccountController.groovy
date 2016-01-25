package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import net.hedtech.banner.webtailor.WebTailorUtility

class UpdateAccountController {

    def directDepositAccountService
    def bankRoutingInfoService
    def directDepositAccountCompositeService
    def directDepositConfigurationService

    def createAccount() {
        def map = request?.JSON ?: params
        map.pidm = ControllerUtility.getPrincipalPidm()

        // default values for a new Direct Deposit account
        map.id = null
        map.documentType = 'D'
        map.intlAchTransactionIndicator = 'N'

        def configStatus = directDepositConfigurationService.getParam(directDepositConfigurationService.SHOW_USER_PRENOTE_STATUS, 'Y')
        map.status = (configStatus == 'Y') ? 'P' : 'A'

        log.debug("trying to create acct: "+ map.bankAccountNum)

        try {
            JSON.use( 'deep' ) {
                render directDepositAccountCompositeService.addorUpdateAccount(map) as JSON
            }
        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }

    def updateAccount() {
        def map = request?.JSON ?: params

        try {
            directDepositAccountService.syncApAndHrAccounts(map)
            
            render directDepositAccountService.update(map) as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }
    
    def reorderAccounts() {
        def map = request?.JSON ?: params

        try {
            render directDepositAccountCompositeService.rePrioritizeAccounts(map, map.newPosition) as JSON

            directDepositAccountService.syncApAndHrAccounts(map)

        } catch (ApplicationException e) {
            def arrayResult = [];
            arrayResult[0] = returnFailureMessage(e)
            
            render arrayResult as JSON
        }
    }
    
    def reorderAllAccounts() {
        def map = request?.JSON ?: params

        try {
            render directDepositAccountCompositeService.reorderAccounts(map) as JSON

        } catch (ApplicationException e) {
            def arrayResult = [];
            arrayResult[0] = returnFailureMessage(e)
            
            render arrayResult as JSON
        }
    }

    def deleteAccounts() {
        def map = request?.JSON ?: params

        try {
            def accounts = [:]
            def model = [:]
            accounts = directDepositAccountService.setupAccountsForDelete(map)
            def result = directDepositAccountService.delete(accounts.toBeDeleted)
            
            model.messages = accounts.messages
            model.messages.add(result)
            
            render model.messages as JSON

        } catch (ApplicationException e) {
            def arrayResult = [];
            arrayResult[0] = returnFailureMessage(e)
            
            render arrayResult as JSON
        }
    }

    def getBankInfo() {
        def map = request?.JSON ?: params
        
        log.debug("trying to fetch bank: "+ map.bankRoutingNum)
        
        try {
            render bankRoutingInfoService.validateRoutingNumber(map.bankRoutingNum)[0] as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }
    
    def validateAccountNum() {
        def model = [:]
        def map = request?.JSON ?: params
        
        log.debug("validating acct num: "+ map.bankAccountNum)
        
        try {
            model.failure = false
            directDepositAccountService.validateAccountNumFormat(map.bankAccountNum)
            render model as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }
    
    def getDisclaimerText() {
        def model = [:]
        
        log.debug("fetching disclaimer text")
        
        model.disclaimer = WebTailorUtility.getInfoText("XeDirectDeposit", "XE_DIRECT_DEPOSIT")
        
        if(!model.disclaimer){
            model.failure = true
            
            log.error("Error: Disclaimer text could not be retrieved")
        }
        
        render model as JSON
    }

    def  returnFailureMessage(ApplicationException  e) {
        def model = [:]
        model.failure = true
        log.error(e)
        try {
            def extractError = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) })
            model.message = extractError.message + (extractError.errors ? " "+ extractError.errors : "")
            return model
        } catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }


}
