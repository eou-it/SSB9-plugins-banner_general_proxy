package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility

// TODO: need below commented imports?
//import net.hedtech.banner.i18n.LocalizeUtil
//import org.apache.log4j.Logger
//import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

class AccountListingController  {

    def directDepositAccountService


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

        render model as JSON
    }

}
