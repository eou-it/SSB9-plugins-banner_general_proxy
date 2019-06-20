/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.security.XssSanitizer
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Controller for Proxy Management
 */
class ProxyManagementController {

    def generalSsbProxyManagementService

    static defaultAction = 'landingPage'

    def landingPage() {

        render view: "proxyManagement"
    }


    /*
     Returns the list of Proxies.
    */
    def getProxies() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        def proxies = generalSsbProxyManagementService.getProxyList(pidm)

        render proxies as JSON
    }

    /*
     Returns the single instance of the Proxy.
    */
    def getProxy() {
        def proxy
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        def gidm = XssSanitizer.sanitize(params.gidm)

        proxy = generalSsbProxyManagementService.getProxyProfile(gidm, pidm)

        render proxy as JSON
    }
}