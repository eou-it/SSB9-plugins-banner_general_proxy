package net.hedtech.banner.general

import net.hedtech.banner.general.system.InstitutionalDescription
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

class ControllerUtility {

    static def log = Logger.getLogger('net.hedtech.banner.employee.ControllerUtility')

    def static getPrincipalPidm() {
        try {
            return SecurityContextHolder?.context?.authentication?.principal?.pidm
        } catch (MissingPropertyException it) {
            log.error("principal lacks a pidm - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }


    public static getCurrencyCode() {
        def currencyCode
        def session = RequestContextHolder?.currentRequestAttributes()?.request?.session

        if (session?.getAttribute("baseCurrencyCode")) {
            currencyCode = session.getAttribute("baseCurrencyCode")
        } else {
            currencyCode = InstitutionalDescription.fetchByKey()?.baseCurrCode
            session.setAttribute("baseCurrencyCode", currencyCode)
        }

        return currencyCode
    }

}
