package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility

class AccountListingController  {

    def directDepositAccountService
    def directDepositPayrollHistoryService

    private def findPerson() {
        return PersonUtility.getPerson(ControllerUtility.getPrincipalPidm())
    }

    def getMyAccounts() {
        def person = findPerson()
        def model = [:]

        if (person) {
            try {
                model = directDepositAccountService.getActiveApAccounts(person.pidm)
            } catch (ApplicationException e) {
                render returnFailureMessage(e) as JSON
            }
        }

        JSON.use("deep") {
            render model as JSON
        }
    }
    
    def getLastPayDateInfo() {
        def model = [:]
        def pidm = ControllerUtility.getPrincipalPidm()
        
        model = directDepositPayrollHistoryService.getLastPayDistribution(pidm)
        
        render model as JSON
    }

}
