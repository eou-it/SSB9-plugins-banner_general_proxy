/********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.proxy.api.GlobalProxyManagementApi
import net.hedtech.banner.proxy.api.ProxyLandingPageApi
import org.springframework.security.core.context.SecurityContextHolder

@Slf4j
@Transactional
class GlobalProxyService {
    def personEmailService
    def generalSsbProxyService
    def personIdentificationNameCurrentService
    def sessionFactory                     // injected by Spring
    def dataSource

    def doesUserHaveActivePreferredEmail(pidm) {
        def userPreferredEmail = personEmailService.findPreferredEmailAddress(pidm)
        def doesUserHaveActivePreferredEmail = userPreferredEmail != null
        doesUserHaveActivePreferredEmail
    }

    def getUserActivePreferredEmailFirstNameAndLastName(pidm) {
        def userPreferredEmail = personEmailService?.findPreferredEmailAddress(pidm)?.emailAddress
        def bannerPersonName = personIdentificationNameCurrentService?.getCurrentNameByPidm(pidm)
        return [email: userPreferredEmail, firstName: bannerPersonName?.firstName, lastName: bannerPersonName?.lastName]
    }

    def getRelationshipOptions(pidm) {
        def relationships
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = GlobalProxyManagementApi.RELATIONSHIP_OPTION_LIST
        try {
            sql.call(sqlText, [pidm, Sql.VARCHAR])
                    { relationshipsJson ->
                        relationships = relationshipsJson
                    }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was retrieving the relationship options for a new relationship: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        relationships ? new JsonSlurper().parseText(relationships) : [:]
    }

    def getDataModelOnRelationshipChange(def params) {
        def data = [:]
        data.pages = getProxyPages(params)
        data
    }

    def getProxyPages(def params) {

        def proxyPages = ""

        def sqlText = GlobalProxyManagementApi.GLOBAL_PROXY_PAGES
        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [params.p_retp_code, Sql.VARCHAR]) {
                proxyPagesJson ->
                    proxyPages = proxyPagesJson
            }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was getting the available pages for a new relationship: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        def authPages = new JsonSlurper().parseText(proxyPages)
        authPages?.pages?.each {
            it?.auth = (it.auth == "Y") ? true : false
        }

        return authPages
    }

    def checkIfGlobalProxyTargetIsValid(params) {
        def returnJson = ''
        def parsedJson

        def sqlText = GlobalProxyManagementApi.CHECK_IF_GLOBAL_PROXY_ACCESS_TARGET_IS_VALID

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [params.targetId, Sql.VARCHAR]) {
                json ->
                    returnJson = json
            }
        }
        catch (e) {
            log.error("The following error occurred while checking if a Global Proxy targeted relationship is valid: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        parsedJson = new JsonSlurper()?.parseText(returnJson)
        return parsedJson
    }

    def deleteProxy(params) {
        def errorStatus = ''

        def sqlText = GlobalProxyManagementApi.DELETE_GLOBAL_PROXY_RELATIONSHIP

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [params.gidm, params.personPidm, params.globalPidm, Sql.VARCHAR]) {
                returnParam ->
                    errorStatus = returnParam
            }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was attempting to delete a relationship: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        return errorStatus
    }

    def createProxy(params) {
        def returnMessage = [:]
        def errorStatus = ''
        def errorMsg = ''

        def sqlText = GlobalProxyManagementApi.CREATE_GLOBAL_PROXY()

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [params.gidm, params.globalPidm, params.retp, params.targetBannerId, Sql.VARCHAR, Sql.VARCHAR]) {
                returnErrorStatus, returnErrorMsg ->
                    errorStatus = returnErrorStatus
                    errorMsg = returnErrorMsg

            }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was attempting to create a relationship: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        returnMessage = [errStatus: errorStatus, errMsg: errorMsg]
        return returnMessage
    }

    def getGlobalProxyGidm(params) {
        def gidm = ''

        def sqlText = GlobalProxyManagementApi.GET_GLOBAL_PROXY_GIDM()

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [params.globalPidm, params.email, params.lastName, params.firstName, Sql.VARCHAR]) {
                returnGidm ->
                    gidm = returnGidm
            }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was attempting to get the Global Proxy Gidm: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        return gidm
    }

    def getTargetPidmFromCaseInsensitiveId(caseInsensitiveBannerId) {
        def pidm = "NULL"

        def sqlText = GlobalProxyManagementApi.GET_TARGET_PIDM_FROM_CASE_INSENSITIVE_ID

        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [caseInsensitiveBannerId, Sql.VARCHAR]) {
                returnPidm ->
                    pidm = returnPidm
            }
        }
        catch (e) {
            log.error("The following error occurred while the Global Proxy was attempting to get the preferred name of a targeted student to proxy: " + e.printStackTrace())
            throw new ApplicationException(GlobalProxyService.class, e)
        }

        return pidm
    }

    def getStudentListForGlobalProxy(def gidm) {

        def studentList = ""

        def sqlText = ProxyLandingPageApi.STUDENT_LIST_FOR_PROXY

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [gidm, Sql.VARCHAR
        ]) { studentListJson ->
            studentList = studentListJson
        }

        def studentsListMap = new JsonSlurper().parseText(studentList)

        studentsListMap << generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        studentsListMap.students.active.each { it ->
            def pidm = PersonUtility.getPerson(it.id).pidm
            it.bannerId = it.id
            it.id = generalSsbProxyService.getToken(it.id)
            def pages = generalSsbProxyService.getProxyPages(gidm, pidm)?.pages
            it.pages = pages
            it.name = PersonUtility.getPreferredNameForProxyDisplay(pidm)
        }

        return studentsListMap
    }

}
