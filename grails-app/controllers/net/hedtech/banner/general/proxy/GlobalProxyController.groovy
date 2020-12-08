/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.configuration.ConfigProperties
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.i18n.MessageHelper
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.SQLException

class GlobalProxyController {
    static defaultAction = 'landingPage'
    def globalProxyService
    def generalSsbProxyService
    def springSecurityService

    private final String STUDENT_LOCATION_CONFIG_NAME = 'globalProxyAccessURL.STUDENTLOCATION'
    private final String CONSTANT_BAN9_PROXY_APP_ID = 'BAN9_PROXY'

    def landingPage() {
        session["studentSsbBaseURL"] = getSSSUrl()

        render view: "globalProxy"
    }

    def getGlobalProxies() {
        def studentList

        log.debug("Get Student List for Proxy")

        def pidm = springSecurityService?.getAuthentication()?.user?.pidm

        if (session["students"] == null) {

            // get idm for the global access user
            if (pidm && !SecurityContextHolder?.context?.authentication?.principal?.gidm) {
                def gidm = generalSsbProxyService.getGIDMfromPidmGlobalAccess(pidm)
                if (gidm) {
                    SecurityContextHolder?.context?.authentication?.principal?.gidm = Integer.valueOf(gidm)
                }
            }
            studentList = getStudentList()
            session["students"] = studentList
        } else {
            studentList = session["students"]
        }

        render studentList as JSON
    }

    def getLoggedInUserHasValidEmailAddress() {
        try {
            def returnMap = checkIfLoggedInUserHasValidEmailAddress()
            render returnMap as JSON
        }
        catch (Exception e) {
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

        } catch (Exception e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def checkIfGlobalProxyAccessTargetIsValid() {
        def params = request?.JSON ?: params
        try {
            def returnData = [:]
            if (isGlobalProxyUserTargetingTheirOwnId(params?.targetId)) {
                returnData.isValidBannerId = "true"
                returnData.isValidToBeProxied = "false"
            } else {
                returnData = globalProxyService.checkIfGlobalProxyTargetIsValid(params)
                if (returnData?.isValidBannerId == "true" && returnData?.isValidToBeProxied == "true") {
                    returnData.preferredName = getPreferredNameBasedOnProxyRule(params?.targetId)
                }
            }
            render returnData as JSON
        } catch (Exception e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteProxy() {
        def proxy = request?.JSON ?: params
        def globalPidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            proxy.personPidm = generalSsbProxyService?.getStudentPidmFromToken(proxy?.token)
            proxy.globalPidm = globalPidm
            proxy.gidm = generalSsbProxyService?.getGIDMfromPidmGlobalAccess(globalPidm)

            def errorStatus = globalProxyService.deleteProxy(proxy)
            if (errorStatus == 'Y') {
                throw new ApplicationException(GlobalProxyController.class, MessageHelper.message("globalProxyManagement.delete.failure"))
            }

            def studentList = getStudentList(proxy.gidm)
            session["students"] = studentList
            render studentList as JSON

        } catch (Exception e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def createGlobalProxyRelationship() {
        try {
            def returnMapValidUserEmailAddress = checkIfLoggedInUserHasValidEmailAddress()

            if(!returnMapValidUserEmailAddress.doesUserHaveActivePreferredEmailAddress){
                throw new ApplicationException(GlobalProxyController.class, MessageHelper.message("globalProxyManagement.message.noActivePreferredEmailAddress"))
            }
            else if (returnMapValidUserEmailAddress.isUsersEmailInUseByAnotherProxy){
                throw new ApplicationException(GlobalProxyController.class, MessageHelper.message("globalProxyManagement.message.emailInUseByProxy"))
            }

            def proxy = request?.JSON ?: params
            def globalPidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            def userEmailFirstNameAndLastName = globalProxyService.getUserActivePreferredEmailFirstNameAndLastName(globalPidm)
            def gidmParams = [globalPidm: globalPidm, email: userEmailFirstNameAndLastName.email, lastName: userEmailFirstNameAndLastName.lastName, firstName: userEmailFirstNameAndLastName.firstName]
            proxy.gidm = globalProxyService.getGlobalProxyGidm(gidmParams)
            proxy.globalPidm = globalPidm

            if (proxy.gidm) {
                def errorInformation = globalProxyService.createProxy(proxy)

                if (errorInformation.errStatus == 'Y') {
                    def errorMessage = errorInformation.errMsg != "PROXYEXISTS" ? "globalProxyManagement.create." + errorInformation.errMsg : "globalProxyManagement.create.failure"
                    throw new ApplicationException(GlobalProxyController.class, MessageHelper.message(errorMessage))
                }

                def studentList = getStudentList(proxy.gidm)
                session["students"] = studentList
                render studentList as JSON
            } else {
                throw new ApplicationException(GlobalProxyController.class, MessageHelper.message("globalProxyManagement.create.failure"))
            }

        } catch (Exception e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    private static boolean isGlobalProxyUserTargetingTheirOwnId(targetId) {
        Integer globalProxyUserPidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        def globalProxyUser = PersonIdentificationName?.fetchBannerPerson(globalProxyUserPidm)
        globalProxyUser?.bannerId == targetId?.trim()
    }

    private String getPreferredNameBasedOnProxyRule(String bannerId) {
        def pidm = globalProxyService.getTargetPidmFromCaseInsensitiveId(bannerId)
        return pidm != "NULL" ? PersonUtility?.getPreferredNameForProxyDisplay(Integer?.valueOf(pidm)) : ""
    }

    private def getSSSUrl() {
        try {
            ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId(STUDENT_LOCATION_CONFIG_NAME, CONSTANT_BAN9_PROXY_APP_ID)
            return configProperties ? configProperties.configValue : null
        } catch (SQLException e) {
            log.warn("Unable to fetch the configuration STUDENTLOCATION " + e.getMessage())
            return ""
        }
    }

    private def getStudentList(gidm = null) {
        def p_proxyIDM = gidm ? gidm : SecurityContextHolder?.context?.authentication?.principal?.gidm
        def studentList = globalProxyService.getStudentListForGlobalProxy(p_proxyIDM)
        return studentList
    }

    private def checkIfLoggedInUserHasValidEmailAddress() {
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        def doesUserHaveActivePreferredEmail
        def isUsersEmailInUseByAnotherProxy = false
        doesUserHaveActivePreferredEmail = pidm ?
                globalProxyService?.doesUserHaveActivePreferredEmail(pidm) :
                false

        if (doesUserHaveActivePreferredEmail) {
            isUsersEmailInUseByAnotherProxy = globalProxyService?.isUsersEmailInUseByAnotherProxy(pidm)
        }


        return [doesUserHaveActivePreferredEmailAddress: doesUserHaveActivePreferredEmail,
                isUsersEmailInUseByAnotherProxy        : isUsersEmailInUseByAnotherProxy]
    }
}
