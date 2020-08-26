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

    def landingPage() {

        render view: "globalProxy"
    }

    def getGlobalProxies() {
        def testMap = [proxies: []]
        render testMap as JSON
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
