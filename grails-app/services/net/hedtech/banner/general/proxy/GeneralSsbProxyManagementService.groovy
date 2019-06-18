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
            }
        }

        return [proxyProfile : proxyProfile]
    }
}
