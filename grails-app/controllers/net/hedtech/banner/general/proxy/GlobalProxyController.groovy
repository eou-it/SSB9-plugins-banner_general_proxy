/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.configuration.ConfigProperties
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.SQLException

/**
 * Controller for Global Proxy.
 */
class GlobalProxyController {
    static defaultAction = 'landingPage'
    def globalProxyService
    def generalSsbProxyManagementService
    def generalSsbProxyService
    def springSecurityService

    def landingPage() {
        session["studentSsbBaseURL"] =  getSSSUrl()

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
                if (gidm){
                    SecurityContextHolder?.context?.authentication?.principal?.gidm = Integer.valueOf(gidm)
                }
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

    /**
     * Get list of options for Relationship select input control.
     */
    def getRelationshipOptions() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def result = globalProxyService.getRelationshipOptions(pidm)
            render result as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
        catch (Exception e) {
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }
    }

    def getDataModelOnRelationshipChange() {
        def params = request?.JSON ?: params
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            params.pidm = pidm

            if (params.alt) {
                params.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)
            }

            def pageData = globalProxyService.getDataModelOnRelationshipChange(params)

            render pageData as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def checkIfGlobalProxyAccessTargetIsValid(){
        def params = request?.JSON ?: params
        try {
            def returnData = [:]
            if (isGlobalProxyUserTargetingTheirOwnId(params?.targetId)){
                returnData.isValidBannerId = "true"
                returnData.isValidToBeProxied = "false"
            }
            else{
                returnData = globalProxyService.checkIfGlobalProxyTargetIsValid(params)
                if (returnData?.isValidBannerId == "true" && returnData?.isValidToBeProxied == "true"){
                    returnData.preferredName = getPreferredNameBasedOnProxyRule(params?.targetId)
                }
            }
            render returnData as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    private boolean isGlobalProxyUserTargetingTheirOwnId(targetId){
        Integer globalProxyUserPidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        def globalProxyUser = PersonIdentificationName?.fetchBannerPerson(globalProxyUserPidm)
        globalProxyUser?.bannerId == targetId?.trim()
    }

    private String getPreferredNameBasedOnProxyRule(String bannerId){
        def bannerPerson = PersonIdentificationName?.fetchBannerPerson(bannerId)
        return PersonUtility?.getPreferredNameForProxyDisplay(bannerPerson?.pidm)
    }

    private def getSSSUrl() {
        try{
            ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId('STUDENTLOCATION','GENERAL_SS')
            return configProperties? configProperties.configValue  : null
        }catch (SQLException e){
            log.warn("Unable to fetch the configuration STUDENTLOCATION "+e.getMessage())
            return ""
        }
    }
}
