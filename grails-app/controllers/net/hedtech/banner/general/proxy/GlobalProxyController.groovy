/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import org.springframework.security.core.context.SecurityContextHolder
/**
 * Controller for Global Proxy.
 */
class GlobalProxyController {
    static defaultAction = 'landingPage'
    def globalProxyService
    def generalSsbProxyService
    def springSecurityService

    def landingPage() {

        render view: "globalProxy"
    }

    def getGlobalProxies() {
        def studentList

        log.debug("Get Student List for Proxy")

        def pidm = springSecurityService?.getAuthentication()?.user?.pidm

        if (session["students"] == null) {

            // get idm for the global access user
            if (pidm && !SecurityContextHolder?.context?.authentication?.principal?.gidm){
                def gidm = generalSsbProxyService.getGIDMfromPidmGlobalAccess(pidm)
                SecurityContextHolder?.context?.authentication?.principal?.gidm = Integer. valueOf(gidm)
            }

            def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
            studentList = generalSsbProxyService.getStudentListForProxy(p_proxyIDM)

            session["students"] = studentList
        }
        else {
            studentList = session["students"]
        }

        render studentList as JSON
    }

    def getLoggedInUserHasActivePreferredEmail() {
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            def doesUserHaveActivePreferredEmail
            doesUserHaveActivePreferredEmail = pidm ?
                    globalProxyService?.doesUserHaveActivePreferredEmail(pidm) :
                    false
            def returnMap = [doesUserHaveActivePreferredEmailAddress: doesUserHaveActivePreferredEmail]
            render returnMap as JSON
        }
        catch (ApplicationException e) {
            ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

}
