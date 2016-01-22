package net.hedtech.banner.general

import grails.converters.JSON

import net.hedtech.banner.DateUtility
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.crossproduct.BankRoutingInfo
import net.hedtech.banner.webtailor.WebTailorUtility

import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.runtime.InvokerHelper

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

            // Do some cleanup to prepare for update
            removeKeyValuePairsNotWantedForUpdate(map)
            fixJSONObjectForCast(map)

            render directDepositAccountService.update(map) as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }
    
    def reorderAccounts() {
        // TODO: this needs to reorder accounts
        def map = request?.JSON ?: params

        try {
            directDepositAccountService.syncApAndHrAccounts(map)
            
            render directDepositAccountService.update(map) as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
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

    /**
     * Certain key/value pairs, most notably those including dates, can get flagged as dirty, and an object that
     * is actually clean from a functional point of view is unnecessarily updated.  Removing such fields prevents
     * this from occurring.
     * @param json JSONObject to fix
     */
    private def removeKeyValuePairsNotWantedForUpdate(JSONObject json) {
        json.remove("lastModified")

        // The bankRoutingInfo object gets flagged as dirty, apparently because it contains its own lastModified
        // name/value pair.  We don't want to update that object here anyway, so remove it.
        json.remove("bankRoutingInfo")
    }

    /**
     * Prepare the values in a JSONObject map to be properly cast to values that can be set on the
     * domain object (i.e. class DirectDepositAccount).
     *
     * To be specific, here's the problem:  ServiceBase.update uses InvokerHelper to set properties from
     * the JSON object onto the domain object.  In doing this, InvokerHelper.setProperties eventually
     * results in the DefaultTypeTransformation.castToType (the class is a Java one not Groovy).  This
     * method does not know how to handle JSONObject.NULL as a true Java null nor a date string as a
     * Date object, which results in exceptions being thrown for these.  In this method we fix the null
     * issue.  We have no date objects that we want to explicitly update, so as of this writing we don't
     * try to fix those.
     * @param json JSONObject to fix
     */
    private def fixJSONObjectForCast(JSONObject json) {
        json.each {entry ->
            // Make JSONObject.NULL a real Java null
            if (entry.value == JSONObject.NULL) {
                entry.value = null

//            If we ever want to fix dates, this is one possible solution
//            } else if (entry.key == "lastModified") {
//                // Make this date string a real Date object
//                entry.value = DateUtility.parseDateString(entry.value, "yyyy-MM-dd'T'HH:mm:ss'Z'")
            }
        }
    }

}
