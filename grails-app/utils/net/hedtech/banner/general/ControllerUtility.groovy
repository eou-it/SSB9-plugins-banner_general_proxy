package net.hedtech.banner.general

import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

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

}
