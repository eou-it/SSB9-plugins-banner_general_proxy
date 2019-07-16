/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
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
            if (!(map?.p_retp_code && isAtLeastOnePageAuthorized(map?.pages))) {
                throw new ApplicationException("", MessageHelper.message("proxyManagement.onSave.REQUIRED"))
            }

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

            Map response = [gidm: map.gidm, failure: false]
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

            Map response = [gidm: map.gidm, failure: false]
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


    def resetProxyPassword() {

        def params = request?.JSON ?: params
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def status = generalSsbProxyManagementService.resetProxyPassword(params.gidm, pidm)

            def response = [resetStatus: status, failure: false]
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
     Returns the list of Cloned Proxies.
    */
    def getClonedProxiesList() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        def proxies = generalSsbProxyManagementService.getProxyClonedList(params)

        render proxies as JSON
    }


    /*
     Returns the list of Cloned Proxies on Create Action.
    */
    def getClonedProxiesListOnCreate() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        def proxies = generalSsbProxyManagementService.getProxyClonedListOnCreate(params)

        render proxies as JSON
    }


    /*
     Get proxy start/stop dates
    */
    def getDataModelOnAuthorizationChange() {
        def params = request?.JSON ?: params
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            params.pidm = pidm
            def authorizations = generalSsbProxyManagementService.getProxyPages(params)

            render authorizations as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getCommunicationLog() {
        // TODO: call Grails service to get the below data.  Some of the fields will need to change, I'm sure, especially
        // those in the "resend" map below.  Then corresponding changes will need to made in the Angular service, etc.
        def mockCommunicationLogData = [
                [
                    transmitDate: '01/11/2019',
                    subject: 'New proxy confirmation 1',
                    actionDate: '04/18/2019',
                    expirationDate: '02/08/2020',
                    resend: [
                            emailAddress: 'blah1@ellucian.com',
                            message: 'This is the message sent to the proxy.'
                    ]
                ],
                [
                    transmitDate: '02/11/2019',
                    subject: 'New proxy confirmation 2',
                    actionDate: '04/18/2019',
                    expirationDate: '02/08/2020',
                    resend: [
                            emailAddress: 'blah2@ellucian.com',
                            message: 'This is the other message sent to the proxy.'
                    ]
                ]
            ]

        render mockCommunicationLogData as JSON
    }

    private isAtLeastOnePageAuthorized(pages) {
        if (!pages) {
            return false;
        }

        def found = pages.find {it?.auth}

        found ? true : false
    }
}