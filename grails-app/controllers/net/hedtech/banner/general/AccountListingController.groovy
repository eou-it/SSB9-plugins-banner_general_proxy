package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility

class AccountListingController  {

    def directDepositAccountService
    def directDepositPayrollHistoryService
    def directDepositAccountCompositeService
    def currencyFormatService


    private def findPerson() {
        return PersonUtility.getPerson(ControllerUtility.getPrincipalPidm())
    }

    def getApAccountsForCurrentUser() {
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
    
    def getHrAccountsForCurrentUser() {
        def person = findPerson()
        def model = [:]

        if (person) {
            try {
                model = directDepositAccountService.getActiveHrAccounts(person.pidm)
            } catch (ApplicationException e) {
                render returnFailureMessage(e) as JSON
            }
        }

        JSON.use("deep") {
            render model as JSON
        }
    }

    def getUserPayrollAllocations() {
        def person = findPerson()

        if (person) {
            try {
                JSON.use('deep') {
                    render directDepositAccountCompositeService.getUserHrAllocations(person.pidm) as JSON
                }
            } catch (ApplicationException e) {
                render returnFailureMessage(e) as JSON
            }
        }
    }

    def getLastPayDateInfo() {
        def model = [:]
        def pidm = ControllerUtility.getPrincipalPidm()

        model = directDepositPayrollHistoryService.getLastPayDistribution(pidm)

        model.totalNet = formatCurrency(model.totalNet)

        model.docAccts.each { it ->
            it.net = formatCurrency(it.net)
        }

        render model as JSON
    }

    /**
     * Formats a Double or BigDecimal to currency.  This is null safe.
     */
    private formatCurrency(amount) {
        def formattedAmount

        if (amount instanceof BigDecimal
                || amount instanceof Double) {
            formattedAmount = currencyFormatService.format(ControllerUtility.getCurrencyCode(),
                    (amount instanceof BigDecimal ? amount : BigDecimal.valueOf(amount)))
        }

        return formattedAmount
    }
}
