/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
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

    /*
     Delete Proxy.
     */
    def deleteProxy() {
        def proxy = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        proxy.pidm = pidm

        try {
            generalSsbProxyManagementService.deleteProxyProfile(proxy)

            def proxies = generalSsbProxyManagementService.getProxyList(pidm)

            render proxies as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /*
      Create Proxy.
    */
    def createProxy() {

        def map = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        map.pidm = pidm

        try {
            def gidm = generalSsbProxyManagementService.createProxyProfile(map)
            def data = [:]
            data."gidm" = gidm
            render data as JSON
        }catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /*
      Create Full Proxy Profile with the Authorization Pages .
    */
    def createUpdateProxy(){

        def map = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        map.pidm = pidm

        try {
            def gidm = generalSsbProxyManagementService.createProxyProfile(map)
            map.gidm = gidm

            generalSsbProxyManagementService.updateProxyProfile(map)
            map?.pages?.each{
                def authParams = [:]
                authParams."gidm" = map.gidm
                authParams."pidm" = pidm
                authParams."page" = it.url
                authParams."checked" = it.auth ? "TRUE":"FALSE"
                generalSsbProxyManagementService.manageProxyPagesAuthorization(authParams)
            }

            Map response = [gidm: map.gidm, failure: false, message: "PROXY-CREATED"]
            render response as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
        catch(Exception e){
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }
    }

    /*
     * Get proxy start/stop dates
     */
    def getDataModelOnRelationshipChange() {
        def params = request?.JSON ?: params
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            params.pidm = pidm
            def startStopDates = generalSsbProxyManagementService.getDataModelOnRelationshipChange(params)

            render startStopDates as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /*
     Updates Proxy.
    */
    def updateProxy(){

        def map = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        map.pidm = pidm

        try {

            generalSsbProxyManagementService.updateProxyProfile(map)
            map?.pages?.each{
                def authParams = [:]
                authParams."gidm" = map.gidm
                authParams."pidm" = pidm
                authParams."page" = it.url
                authParams."checked" = it.auth ? "TRUE":"FALSE"
                generalSsbProxyManagementService.manageProxyPagesAuthorization(authParams)
            }

            Map response = [gidm: map.gidm, failure: false, message: "PROXY-UPDATED"]
            render response as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
        catch(Exception e){
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }
    }


    /**
     * Get list of options for Relationship select input control.
     */
    def getRelationshipOptions() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def result = generalSsbProxyManagementService.getRelationshipOptions(pidm)

            render result as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
        catch(Exception e){
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }
    }
}