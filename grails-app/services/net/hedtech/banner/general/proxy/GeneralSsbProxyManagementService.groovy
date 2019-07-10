/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.OutParameter
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.proxy.api.ProxyManagementApi
import oracle.jdbc.driver.OracleTypes
import java.sql.SQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Transactional
class GeneralSsbProxyManagementService {
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring

    static Logger logger = LoggerFactory.getLogger(GeneralSsbProxyManagementService.class)

    def getProxyList(def pidm) {

        def proxyList
        def sqlText = ProxyManagementApi.PROXY_LIST

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.call(sqlText, [pidm, Sql.VARCHAR
            ]) { proxyListJson ->
                proxyList = proxyListJson
            }
        }catch (e) {
            log.error("ERROR: Could not get the List of Proxies. $e")
            throw e
        }

        def studentsListMap = new JsonSlurper().parseText(proxyList)

        return studentsListMap
    }


    def createProxyProfile(def params) {

        logger.debug('createProxyProfile')
        logger.debug('Parameters: ' + params)

        def errorMsgOut = ""
        def errorStatusOut = ""
        def gidmOut

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def sqlText = ProxyManagementApi.CREATE_PROXY

        try {
            sql.call(sqlText, [params.pidm, params.p_email, params.p_email_verify, params.p_last, params.p_first,
                               Sql.VARCHAR, Sql.VARCHAR, Sql.NUMERIC
            ]){ errorMsg, errorStatus, gidm ->
                errorMsgOut = errorMsg
                errorStatusOut = errorStatus
                gidmOut = gidm
            }
            logger.debug('finished createProxyProfile')
        } catch (SQLException e) {
            logger.error('createProxyProfile() - '+ e)
            def ae = new ApplicationException( GeneralSsbProxyManagementService.class, e )
            throw ae
        } finally {
            //sql?.close()
        }

        if (errorMsgOut && errorStatusOut.equals("Y")){
            throw new ApplicationException("", MessageHelper.message("proxyManagement.onSave." + errorMsgOut))

        }else{
            return gidmOut
        }
    }


    public def getProxyProfile(def gidm, def pidm) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        // special OutParameter for cursor type
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR;
            }
        };

        def sqlText = ProxyManagementApi.PROXY_PROFILE

        def proxyProfile = [:]

        sql.call(sqlText, [CURSOR_PARAMETER, gidm, pidm]) { profile ->
            profile.eachRow() { data ->
                proxyProfile.gidm = data.GPRXREF_PROXY_IDM
                proxyProfile.pidm = data.GPRXREF_PERSON_PIDM
                proxyProfile.p_retp_code = data.GPRXREF_RETP_CODE
                proxyProfile.p_passphrase = data.GPRXREF_PASSPHRASE
                proxyProfile.p_start_date = data.GPRXREF_START_DATE
                proxyProfile.p_stop_date = data.GPRXREF_STOP_DATE
                proxyProfile.p_desc = data.GPRXREF_PROXY_DESC
            }
        }

        def  proxyPages = ""

        sqlText = ProxyManagementApi.PROXY_PAGES

        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [proxyProfile.p_retp_code, gidm, pidm, Sql.VARCHAR
        ]){ proxyPagesJson ->
            proxyPages = proxyPagesJson
        }


        def authPages = new JsonSlurper().parseText(proxyPages)
        

        authPages?.pages.each{
            it?.auth = (it.auth == "Y")? true : false
        }
        proxyProfile << authPages

        def proxyUiRules = [:]
        sqlText = ProxyManagementApi.PROXY_PROFILE_UI_RULES

        sql.call(sqlText, [proxyProfile.p_retp_code, Sql.VARCHAR, Sql.VARCHAR ])
                { show_p_passphrase, show_p_reset_pin ->

                    if (show_p_passphrase.equals("Y")) {
                        proxyUiRules."p_passphrase" = [visible: true]
                    }

                    if (show_p_reset_pin.equals("Y")) {
                        proxyUiRules."p_reset_pin" = [visible: true]
                    }
                }

        return [proxyProfile : proxyProfile, proxyUiRules : proxyUiRules]
    }

    def deleteProxyProfile(def params) {

        logger.debug('deleteProxyProfile')
        logger.debug('Parameters: ' + params)

        def errorMsgOut = ""
        def errorStatusOut = ""

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def sqlText = ProxyManagementApi.DELETE_PROXY

        try {
            sql.call(sqlText, [params.pidm, params.gidm,
                               Sql.VARCHAR, Sql.VARCHAR
            ]){ errorMsg, errorStatus ->
                errorMsgOut = errorMsg
                errorStatusOut = errorStatus
            }
            logger.debug('finished deleteProxyProfile')
        } catch (SQLException e) {
            logger.error('deleteProxyProfile() - '+ e)
            def ae = new ApplicationException( GeneralSsbProxyManagementService.class, e )
            throw ae
        } finally {
            //sql?.close()
        }

        if (errorMsgOut && errorStatusOut.equals("Y")) {
            throw new ApplicationException("", MessageHelper.message("proxyManagement.onDelete." + errorMsgOut))
        }
    }

    def getProxyStartStopDates(relationshipCode) {

        def startStopDates = [:]
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = ProxyManagementApi.PROXY_START_STOP_DATES

        sql.call(sqlText, [relationshipCode, Sql.TIMESTAMP, Sql.TIMESTAMP ])
                { start_date, stop_date ->

                    startStopDates.startDate = start_date
                    startStopDates.stopDate  = stop_date
                }

        return startStopDates
    }

    // Updates Proxy Profile Without Authorization Pages
    def updateProxyProfile(def params) {

        logger.debug('updateProxyProfile')
        logger.debug('Parameters: ' + params)

        def  errorMsgOut = ""
        def errorStatusOut = ""

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def sqlText = ProxyManagementApi.UPDATE_PROXY

        try {
            sql.call(sqlText, [params.pidm, params.gidm, params.p_retp_code, params.p_desc,
                               params.p_start_date, params.p_stop_date, params.p_passphrase,
                               Sql.VARCHAR, Sql.VARCHAR
            ]){ errorMsg, errorStatus ->
                errorMsgOut = errorMsg
                errorStatusOut = errorStatus
            }
            logger.debug('finished updateProxyProfile')
        } catch (SQLException e) {
            logger.error('updateProxyProfile() - '+ e)
            def ae = new ApplicationException(GeneralSsbProxyManagementService.class, e )
            throw ae
        } finally {
            //sql?.close()
        }

        if (errorMsgOut && errorStatusOut.equals("Y")){
            throw new ApplicationException("", MessageHelper.message("proxyManagement.onUpdate." + errorMsgOut))
        }else if(errorMsgOut && errorStatusOut.equals("N")){
            return MessageHelper.message("proxy.personalinformation.onSave." + errorMsgOut)
        }else{
            return ""
        }

    }

    def manageProxyPagesAuthorization(def params) {

        def sqlText = ProxyManagementApi.MANAGE_AUTHORIZATION

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [params.gidm, params.pidm,
                           params.page, params?.checked?.toString()?.toUpperCase()
        ])
    }

    def getProxyPages(def params) {

        def  proxyPages = ""

        def sqlText = ProxyManagementApi.PROXY_PAGES

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [params.p_retp_code, params.gidm, params.pidm, Sql.VARCHAR
        ]){ proxyPagesJson ->
            proxyPages = proxyPagesJson
        }

        def authPages = new JsonSlurper().parseText(proxyPages)
        authPages?.pages.each{
            it?.auth = (it.auth == "Y")? true : false
        }

        return authPages
    }

    def getDataModelOnRelationshipChange(def params){

        def data = [:]

        data.pages = getProxyPages(params)
        data.dates = getProxyStartStopDates(params.p_retp_code)

        data

    }

    def getRelationshipOptions(pidm) {

        def relationships
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = ProxyManagementApi.RELATIONSHIP_OPTION_LIST

        sql.call(sqlText, [pidm, Sql.VARCHAR])
                { relationshipsJson ->
                    relationships = relationshipsJson
                }

        relationships ? new JsonSlurper().parseText(relationships) : [:]
    }

    def resetProxyPassword(gidm, pidm) {

        def status
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = ProxyManagementApi.RESET_PROXY_PASSWORD

        sql.call(sqlText, [gidm, pidm, Sql.VARCHAR])
                { resetStatus ->
                    status = resetStatus
                }

        status
    }

    /*
      Gets the list of Proxies for the Student to clone from.
    */
    def getProxyClonedList(def params) {

        def proxyClonedList
        def sqlText = ProxyManagementApi.PROXY_CLONE_LIST

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.call(sqlText, [params.gidm, params.pidm, params.p_retp_code,  Sql.VARCHAR
            ]) { proxyListJson ->
                proxyClonedList = proxyListJson
            }
        }catch (e) {
            log.error("ERROR: Could not get the List of Cloned Proxies. $e")
            throw e
        }

        def studentsListMap = new JsonSlurper().parseText(proxyClonedList)

        return studentsListMap
    }


    /*
     Gets the List of Authorization Pages for the Cloned Proxy Identity.
     */
    def getDataModelOnAuthorizationChange(params) {
        def data = [:]
        data.pages = getProxyPages(params)
        data
    }


    /*
     Gets the list of Proxies for the Student to add.
   */
    def getProxyAddList(def params) {

        def proxyAddList
        def sqlText = ProxyManagementApi.BWGKPXYM_PROXYMGMT_ADD_LIST

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.call(sqlText, [params.pidm, Sql.VARCHAR
            ]) { proxyAddListJson ->
                proxyAddList = proxyAddListJson
            }
        }catch (e) {
            log.error("ERROR: Could not get the List of Cloned Proxies. $e")
            throw e
        }

        def studentsAddListMap = new JsonSlurper().parseText(proxyAddList)

        return studentsAddListMap
    }
}
