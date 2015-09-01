package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.exceptions.ApplicationException
//import net.hedtech.banner.i18n.LocalizeUtil
//import org.apache.log4j.Logger
//import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

class AccountListingController  {

    def directDepositAccountService
    def getMyAccounts() {
        def model = [:]

        try {
            model = directDepositAccountService.getActiveApAccounts(38010)
        } catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }

        render model as JSON
    }

}
