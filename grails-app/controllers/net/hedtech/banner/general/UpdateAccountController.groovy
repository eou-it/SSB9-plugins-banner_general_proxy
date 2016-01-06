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
    
    def createAccount() {
        def map = request?.JSON ?: params
        map.pidm = ControllerUtility.getPrincipalPidm()

        // default values for a new Direct Deposit account
        map.id = null
        map.status = 'P'
        map.documentType = 'D'
        map.intlAchTransactionIndicator = 'N'

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
            fixJSONObjectForCast(map)

            render directDepositAccountService.update(map) as JSON

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
        }
    }

    def deleteAccounts() {
        def map = request?.JSON ?: params

        try {
            render directDepositAccountService.delete(map)

        } catch (ApplicationException e) {
            render returnFailureMessage(e) as JSON
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
            model.message = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) }).message
            return model
        } catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }

    /**
     * Prepare the values in a JSONObject map to be properly cast to values that can be set on the
     * domain object (i.e. class DirectDepositAccount).
     *
     * To be specific, here's the problem:  ServiceBase.update uses InvokerHelper to set properties from
     * the JSON object onto the domain object.  In doing this, InvokerHelper.setProperties eventually
     * results in the DefaultTypeTransformation.castToType (the class is a Java one not Groovy).  This
     * method does not know how to handle JSONObject.NULL as a true Java null nor a data string as a
     * Date object, which results in exceptions being thrown for these.  In this method we fix those
     * values to be of a type that castToType can handle.
     * @param json A JSONObject
     */
    private def fixJSONObjectForCast(JSONObject json) {
        json.each {entry ->
            // Make JSONObject.NULL a real Java null
            if (entry.value == JSONObject.NULL) {
                entry.value = null
            } else if (entry.key == "lastModified") {
                // Make this date string a real Date object
                entry.value = DateUtility.parseDateString(entry.value, "yyyy-MM-dd'T'HH:mm:ss'Z'")
            } else if (entry.key == "bankRoutingInfo") {
                def bankRoutingInfoObject = new BankRoutingInfo()

                fixJSONObjectForCast(entry.value)
                // TODO: remove this debug code
                try {
                    def junka = entry
                    def junkb = entry.value
//                    def junk0 = directDepositAccountService.fetch('BankRoutingInfo', entry.value.id, log)
//                    directDepositAccountService.setDomainClass(BankRoutingInfo.class)
//                    def junk0 = directDepositAccountService.fetch('BankRoutingInfo', '6', log)
                    def junk0 = directDepositAccountService.fetch(BankRoutingInfo.class, '6', log)
                    def junk = junk0?.getDirtyPropertyNames()
                    def junk2 = junk0?.getDirtyPropertyNames()?.findAll { entry.value."$it" instanceof Date }?.size()
                    def junk3 = junk0?.getPersistentValue('bankRoutingInfo')
                    def junk4
                } catch (Exception e) {
                    e.printStackTrace()
                }
                // END debug code

                use(InvokerHelper) {
                    bankRoutingInfoObject.setProperties(entry.value)
                }

                entry.value = bankRoutingInfoObject
            }
        }
    }

}
