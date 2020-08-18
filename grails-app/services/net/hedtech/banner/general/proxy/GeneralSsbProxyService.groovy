/********************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.proxy.api.ProxyLandingPageApi
import net.hedtech.banner.proxy.api.ProxyPersonalInformationApi
import net.hedtech.banner.proxy.api.PinManagementApi

import net.hedtech.banner.i18n.MessageHelper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import oracle.jdbc.driver.OracleTypes
import groovy.sql.OutParameter

import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat

import net.hedtech.banner.general.system.State
import net.hedtech.banner.general.system.Nation
import net.hedtech.banner.general.system.County

import net.hedtech.banner.exceptions.ApplicationException

import net.hedtech.banner.general.person.PersonUtility

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import grails.gorm.transactions.Transactional

@Transactional
class GeneralSsbProxyService {
    static Logger logger = LoggerFactory.getLogger(GeneralSsbProxyService.class)
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def grailsApplication                  // injected by Spring
    def preferredNameService
    def springSecurityService


    /**
     * This methods defines the p_token authentication as passed to the proxy
     * 1. Valid Token - redirect to the new pin
     * 2. Valid Toke after Pin was established- login action
     * 3. Error (wrong parameter or altered token) - login screen
     */
    public def setProxy(def token) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def login
        def gidm
        def actionVerify
        def doPin
        def msg
        def error

        def sqlText = PinManagementApi.SET_PROXY
        sql.call(sqlText, [token, null, Sql.NUMERIC, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { gidmOut, actionVerifyOut, pinOut, msgOut, loginOut, errorOut  ->
                    login = loginOut
                    gidm = gidmOut
                    actionVerify = actionVerifyOut
                    doPin = pinOut
                    msg = msgOut
                    error = errorOut
                }

        RequestContextHolder.currentRequestAttributes().getSession()["gidm"] = gidm

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y"), gidm: gidm]
    }


    public def setProxyVerify(def token, def verify, def gidm) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def login
        def actionVerify
        def doPin
        def msg
        def error
        def errorStatus

        def sqlText = PinManagementApi.SET_PROXY
        sql.call(sqlText, [token, verify, Sql.NUMERIC, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { gidmOut, actionVerifyOut, pinOut, msgOut, loginOut, errorOut  ->
                    login = loginOut
                    //gidm = gidmOut
                    actionVerify = actionVerifyOut
                    doPin = pinOut
                    msg = msgOut
                    error = errorOut
                }

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y"), gidm: gidm]
    }


    public def savePin(def p_proxyIDM, def p_pin1, def p_pin2, def p_email, def p_pin_orig) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def msg
        def error
        def errorStatus

        def sqlText = PinManagementApi.SAVE_PIN
        sql.call(sqlText, [p_email, p_pin_orig, p_proxyIDM,
                           p_pin1, p_pin2,
                           p_pin1,
                           p_pin1, p_pin1,
                           p_pin1,
                           p_pin2,
                           p_pin1, p_pin1,
                           p_pin1,
                           p_pin1,
                           p_proxyIDM, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { errorOut, msgOut, errorStatusOut ->
                    error = errorOut
                    msg = msgOut
                    errorStatus = errorStatusOut
                }


        return [errorStatus: errorStatus.equals("Y"), message: msg, error: error, gidm: p_proxyIDM]

    }


    public def getPersonalInformation(def gidm) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        // special OutParameter for cursor type
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR;
            }
        };

        def sqlText = ProxyPersonalInformationApi.PROXY_PERSONAL_INFORMATION

        def proxyProfile = [:]

        sql.call(sqlText, [CURSOR_PARAMETER, gidm]) { profile ->
            profile.eachRow() { data ->
                proxyProfile.p_name_prefix = data.GPBPRXY_NAME_PREFIX
                proxyProfile.p_first_name = data.GPBPRXY_FIRST_NAME
                proxyProfile.p_mi = data.GPBPRXY_MI
                proxyProfile.p_surname_prefix = data.GPBPRXY_SURNAME_PREFIX
                proxyProfile.p_last_name = data.GPBPRXY_LAST_NAME
                proxyProfile.p_name_suffix = data.GPBPRXY_NAME_SUFFIX
                proxyProfile.p_pref_first_name = data.GPBPRXY_PREF_FIRST_NAME
                proxyProfile.p_email_address = data.GPBPRXY_EMAIL_ADDRESS
                proxyProfile.p_ctry_code_phone = data.GPBPRXY_CTRY_CODE_PHONE
                proxyProfile.p_phone_area = data.GPBPRXY_PHONE_AREA
                proxyProfile.p_phone_number = data.GPBPRXY_PHONE_NUMBER
                proxyProfile.p_phone_ext = data.GPBPRXY_PHONE_EXT
                proxyProfile.p_house_number = data.GPBPRXY_HOUSE_NUMBER
                proxyProfile.p_street_line1 = data.GPBPRXY_STREET_LINE1
                proxyProfile.p_street_line2 = data.GPBPRXY_STREET_LINE2
                proxyProfile.p_street_line3 = data.GPBPRXY_STREET_LINE3
                proxyProfile.p_street_line4 = data.GPBPRXY_STREET_LINE4
                proxyProfile.p_city = data.GPBPRXY_CITY
                proxyProfile.p_stat_code = State.findByCode(data.GPBPRXY_STAT_CODE)?: null
                proxyProfile.p_zip = data.GPBPRXY_ZIP
                proxyProfile.p_natn_code = Nation.findByCode(data.GPBPRXY_NATN_CODE)?: null
                proxyProfile.p_cnty_code = County.findByCode(data.GPBPRXY_CNTY_CODE)?: null
                proxyProfile.p_sex = data.GPBPRXY_SEX
                proxyProfile.p_birth_date = data.GPBPRXY_BIRTH_DATE
                proxyProfile.p_ssn = data.GPBPRXY_SSN
                proxyProfile.p_opt_out_adv_date = (data.GPBPRXY_OPT_OUT_ADV_DATE==null) ? false: true
            }

        }

        def proxyUiRules = [:]
        sqlText = ProxyPersonalInformationApi.PROXY_PROFILE_UI_RULES

        sql.call(sqlText, [gidm, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR ])
                { show_p_name_prefix, show_p_mi, show_p_surname_prefix,
                  show_p_name_suffix, show_p_pref_first_name, show_p_phone_area,
                  show_p_phone_number, show_p_phone_ext, show_p_ctry_code_phone,
                  show_p_house_number, show_p_street_line1,show_p_street_line2,show_p_street_line3,show_p_street_line4,
                  show_p_city, show_p_stat_code, show_p_zip, show_p_cnty_code, show_p_natn_code,
                  show_p_sex, show_p_birth_date, show_p_ssn, show_p_opt_out_adv_date ->


                    proxyUiRules."p_name_prefix" = [fieldLength: 20]
                    if (show_p_name_prefix.equalsIgnoreCase("V")){
                        proxyUiRules."p_name_prefix".putAll([visible: true, required : false])
                    }else if(show_p_name_prefix.equalsIgnoreCase("N")){
                        proxyUiRules."p_name_prefix".putAll([visible: false, required : false])
                    }else if(show_p_name_prefix.equalsIgnoreCase("Y")){
                        proxyUiRules."p_name_prefix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_first_name" = [fieldLength: 60, visible: true, required : true]

                    proxyUiRules."p_mi" = [fieldLength: 60]
                    if (show_p_mi.equalsIgnoreCase("V")){
                        proxyUiRules."p_mi".putAll([visible: true, required : false])
                    }else if(show_p_mi.equalsIgnoreCase("N")){
                        proxyUiRules."p_mi".putAll([visible: false, required : false])
                    }else if(show_p_mi.equalsIgnoreCase("Y")){
                        proxyUiRules."p_mi".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_last_name" = [fieldLength: 60, visible: true, required : true]

                    proxyUiRules."p_surname_prefix" = [fieldLength: 60]
                    if (show_p_surname_prefix.equalsIgnoreCase("V")){
                        proxyUiRules."p_surname_prefix".putAll([visible: true, required : false])
                    }else if(show_p_surname_prefix.equalsIgnoreCase("N")){
                        proxyUiRules."p_surname_prefix".putAll([visible: false, required : false])
                    }else if(show_p_surname_prefix.equalsIgnoreCase("Y")){
                        proxyUiRules."p_surname_prefix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_name_suffix" = [fieldLength: 20]
                    if (show_p_name_suffix.equalsIgnoreCase("V")){
                        proxyUiRules."p_name_suffix".putAll([visible: true, required : false])
                    }else if(show_p_name_suffix.equalsIgnoreCase("N")){
                        proxyUiRules."p_name_suffix".putAll([visible: false, required : false])
                    }else if(show_p_name_suffix.equalsIgnoreCase("Y")){
                        proxyUiRules."p_name_suffix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_pref_first_name" = [fieldLength: 60]
                    if (show_p_pref_first_name.equalsIgnoreCase("V")){
                        proxyUiRules."p_pref_first_name".putAll([visible: true, required : false])
                    }else if(show_p_pref_first_name.equalsIgnoreCase("N")){
                        proxyUiRules."p_pref_first_name".putAll([visible: false, required : false])
                    }else if(show_p_pref_first_name.equalsIgnoreCase("Y")){
                        proxyUiRules."p_pref_first_name".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_email_address" = [fieldLength: 75, visible: true, required : true]

                    proxyUiRules."p_phone_area" = [fieldLength: 6]
                    if (show_p_phone_area.equalsIgnoreCase("V")){
                        proxyUiRules."p_phone_area".putAll([visible: true, required : false])
                    }else if(show_p_phone_area.equalsIgnoreCase("N")){
                        proxyUiRules."p_phone_area".putAll([visible: false, required : false])
                    }else if(show_p_phone_area.equalsIgnoreCase("Y")){
                        proxyUiRules."p_phone_area".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_phone_number" = [fieldLength: 12]
                    if (show_p_phone_number.equalsIgnoreCase("V")){
                        proxyUiRules."p_phone_number".putAll([visible: true, required : false])
                    }else if(show_p_phone_number.equalsIgnoreCase("N")){
                        proxyUiRules."p_phone_number".putAll([visible: false, required : false])
                    }else if(show_p_phone_number.equalsIgnoreCase("Y")){
                        proxyUiRules."p_phone_number".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_phone_ext" = [fieldLength: 10]
                    if (show_p_phone_ext.equalsIgnoreCase("V")){
                        proxyUiRules."p_phone_ext".putAll([visible: true, required : false])
                    }else if(show_p_phone_ext.equalsIgnoreCase("N")){
                        proxyUiRules."p_phone_ext".putAll([visible: false, required : false])
                    }else if(show_p_phone_ext.equalsIgnoreCase("Y")){
                        proxyUiRules."p_phone_ext".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_ctry_code_phone" = [fieldLength: 4]
                    if (show_p_ctry_code_phone.equalsIgnoreCase("V")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: true, required : false])
                    }else if(show_p_ctry_code_phone.equalsIgnoreCase("N")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: false, required : false])
                    }else if(show_p_ctry_code_phone.equalsIgnoreCase("Y")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_house_number" = [fieldLength: 10]
                    if (show_p_house_number.equalsIgnoreCase("V")){
                        proxyUiRules."p_house_number".putAll([visible: true, required : false])
                    }else if(show_p_house_number.equalsIgnoreCase("N")){
                        proxyUiRules."p_house_number".putAll([visible: false, required : false])
                    }else if(show_p_house_number.equalsIgnoreCase("Y")){
                        proxyUiRules."p_house_number".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line1" = [fieldLength: 75]
                    if (show_p_street_line1.equalsIgnoreCase("V")){
                        proxyUiRules."p_street_line1".putAll([visible: true, required : false])
                    }else if(show_p_street_line1.equalsIgnoreCase("N")){
                        proxyUiRules."p_street_line1".putAll([visible: false, required : false])
                    }else if(show_p_street_line1.equalsIgnoreCase("Y")){
                        proxyUiRules."p_street_line1".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line2" = [fieldLength: 75]
                    if (show_p_street_line2.equalsIgnoreCase("V")){
                        proxyUiRules."p_street_line2".putAll([visible: true, required : false])
                    }else if(show_p_street_line2.equalsIgnoreCase("N")){
                        proxyUiRules."p_street_line2".putAll([visible: false, required : false])
                    }else if(show_p_street_line2.equalsIgnoreCase("Y")){
                        proxyUiRules."p_street_line2".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line3" = [fieldLength: 75]
                    if (show_p_street_line3.equalsIgnoreCase("V")){
                        proxyUiRules."p_street_line3".putAll([visible: true, required : false])
                    }else if(show_p_street_line3.equalsIgnoreCase("N")){
                        proxyUiRules."p_street_line3".putAll([visible: false, required : false])
                    }else if(show_p_street_line3.equalsIgnoreCase("Y")){
                        proxyUiRules."p_street_line3".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line4" = [fieldLength: 75]
                    if (show_p_street_line4.equalsIgnoreCase("V")){
                        proxyUiRules."p_street_line4".putAll([visible: true, required : false])
                    }else if(show_p_street_line4.equalsIgnoreCase("N")){
                        proxyUiRules."p_street_line4".putAll([visible: false, required : false])
                    }else if(show_p_street_line4.equalsIgnoreCase("Y")){
                        proxyUiRules."p_street_line4".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_city" = [fieldLength: 50]
                    if (show_p_city.equalsIgnoreCase("V")){
                        proxyUiRules."p_city".putAll([visible: true, required : false])
                    }else if(show_p_city.equalsIgnoreCase("N")){
                        proxyUiRules."p_city".putAll([visible: false, required : false])
                    }else if(show_p_city.equalsIgnoreCase("Y")){
                        proxyUiRules."p_city".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_stat_code" = [fieldLength: 3]
                    if (show_p_stat_code.equalsIgnoreCase("V")){
                        proxyUiRules."p_stat_code".putAll([visible: true, required : false])
                    }else if(show_p_stat_code.equalsIgnoreCase("N")){
                        proxyUiRules."p_stat_code".putAll([visible: false, required : false])
                    }else if(show_p_stat_code.equalsIgnoreCase("Y")){
                        proxyUiRules."p_stat_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_zip" = [fieldLength: 30]
                    if (show_p_zip.equalsIgnoreCase("V")){
                        proxyUiRules."p_zip".putAll([visible: true, required : false])
                    }else if(show_p_zip.equalsIgnoreCase("N")){
                        proxyUiRules."p_zip".putAll([visible: false, required : false])
                    }else if(show_p_zip.equalsIgnoreCase("Y")){
                        proxyUiRules."p_zip".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_cnty_code" = [fieldLength: 5]
                    if (show_p_cnty_code.equalsIgnoreCase("V")){
                        proxyUiRules."p_cnty_code".putAll([visible: true, required : false])
                    }else if(show_p_cnty_code.equalsIgnoreCase("N")){
                        proxyUiRules."p_cnty_code".putAll([visible: false, required : false])
                    }else if(show_p_cnty_code.equalsIgnoreCase("Y")){
                        proxyUiRules."p_cnty_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_natn_code" = [fieldLength: 5]
                    if (show_p_natn_code.equalsIgnoreCase("V")){
                        proxyUiRules."p_natn_code".putAll([visible: true, required : false])
                    }else if(show_p_natn_code.equalsIgnoreCase("N")){
                        proxyUiRules."p_natn_code".putAll([visible: false, required : false])
                    }else if(show_p_natn_code.equalsIgnoreCase("Y")){
                        proxyUiRules."p_natn_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_sex" = [fieldLength: 1]
                    if (show_p_sex.equalsIgnoreCase("V")){
                        proxyUiRules."p_sex".putAll([visible: true, required : false])
                    }else if(show_p_sex.equalsIgnoreCase("N")){
                        proxyUiRules."p_sex".putAll([visible: false, required : false])
                    }else if(show_p_sex.equalsIgnoreCase("Y")){
                        proxyUiRules."p_sex".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_ssn" = [fieldLength: 9]
                    if (show_p_ssn.equalsIgnoreCase("V")){
                        proxyUiRules."p_ssn".putAll([visible: true, required : false])
                    }else if(show_p_ssn.equalsIgnoreCase("N")){
                        proxyUiRules."p_ssn".putAll([visible: false, required : false])
                    }else if(show_p_ssn.equalsIgnoreCase("Y")){
                        proxyUiRules."p_ssn".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_birth_date" = [fieldLength: 20]
                    if (show_p_birth_date.equalsIgnoreCase("V")){
                        proxyUiRules."p_birth_date".putAll([visible: true, required : false])
                    }else if(show_p_birth_date.equalsIgnoreCase("N")){
                        proxyUiRules."p_birth_date".putAll([visible: false, required : false])
                    }else if(show_p_birth_date.equalsIgnoreCase("Y")){
                        proxyUiRules."p_birth_date".putAll([visible: true, required : true])
                    }


                    proxyUiRules."p_opt_out_adv_date" = [fieldLength: 1]
                    if (show_p_opt_out_adv_date.equalsIgnoreCase("V")){
                        proxyUiRules."p_opt_out_adv_date".putAll([visible: true, required : false])
                    }else if(show_p_opt_out_adv_date.equalsIgnoreCase("N")){
                        proxyUiRules."p_opt_out_adv_date".putAll([visible: false, required : false])
                    }else if(show_p_opt_out_adv_date.equalsIgnoreCase("Y")){
                        proxyUiRules."p_opt_out_adv_date".putAll([visible: true, required : true])
                    }

                }

        return [proxyProfile : proxyProfile, proxyUiRules : proxyUiRules]
    }


    def updateProxyProfile(def params) {

        logger.debug('updateProxyProfile')
        logger.debug('Parameters: ' + params)

        def  errorMsgOut = ""
        def errorStatusOut = ""
        def emailChangeOut = ""

        def updateRulesErrors = checkProxyProfileDataOnUpdate(params)

        if (updateRulesErrors){
            throw new ApplicationException(GeneralSsbProxyService, updateRulesErrors)
        }

        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def sqlText = ProxyPersonalInformationApi.UPDATE_PROFILE

        String birthdateString = formatAndValidateBirthdate(params.p_birth_date)

        def updatePersonlInformationEmailMessage = MessageHelper.message("proxy.personalinformation.update.email.message")

        try {
            sql.call(sqlText, [p_proxyIDM,params.p_first_name, params.p_last_name,
                               params.p_mi, params.p_surname_prefix, params.p_name_prefix,
                               params.p_name_suffix, params.p_pref_first_name, params.p_phone_area,
                               params.p_phone_number, params.p_phone_ext, params.p_ctry_code_phone,
                               params.p_house_number, params.p_street_line1, params.p_street_line2, params.p_street_line3, params.p_street_line4,
                               params.p_city, params.p_stat_code?.code ?: "", params.p_zip, params.p_cnty_code?.code ?: "", params.p_natn_code?.code ?: "",
                               params.p_sex, birthdateString, params.p_ssn, params.p_opt_out_adv_date ? "Y" : "N", updatePersonlInformationEmailMessage,  params.p_email_address, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR
            ]){ errorMsg, errorStatus, emailChange ->
                errorMsgOut = errorMsg
                errorStatusOut = errorStatus
                emailChangeOut = emailChange
            }
            logger.debug('finished updateProxyProfile')
        } catch (SQLException e) {
            logger.error('updateProxyProfile() - '+ e)
            def ae = new ApplicationException( GeneralSsbProxyService.class, e )
            throw ae
        } finally {
            //sql?.close()
        }

        if (errorMsgOut && errorStatusOut.equals("Y")){
            throw new ApplicationException("", MessageHelper.message("proxy.personalinformation.onSave." + errorMsgOut))
        }else if(errorMsgOut && errorStatusOut.equals("N") && emailChangeOut.equals("Y")){
            return MessageHelper.message("proxy.personalinformation.onSave." + errorMsgOut)
        }else{
            return ""
        }
    }


    /* Updates Audit data on ProxyHistoryOnLogin */
    def updateProxyHistoryOnLogin() {
        logger.debug('starting updateProxyHistoryOnLogin')
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        logger.debug('p_proxyIDM: ' + p_proxyIDM)
        try {
            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            def sqlText = ProxyPersonalInformationApi.STORE_LOGIN_IN_HISTORY

            logger.debug('sqlText: ' + sqlText)

            def msg = MessageHelper.message("proxy.login.accessHistory")

            sql.call(sqlText,
                    [p_proxyIDM, p_proxyIDM, p_proxyIDM, msg = msg ? msg : "Display authorization menu"
                    ])

            logger.debug('finished updateProxyHistoryOnLogin')

        }catch(Exception e) {
            logger.error('Problem setting updateProxyHistoryOnLogin')
            logger.error(e)
        }
    }


    /* Updates Audit data on Proxy Page Access */
    def updateProxyHistoryOnPageAccess(def pidm, def pageName) {
        logger.debug('starting updateProxyHistoryOnPageAccess')
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        logger.debug('p_proxyIDM: ' + p_proxyIDM)
        logger.debug('pidm: ' + pidm)
        if (pidm) {
            try {
                def sql = new Sql(sessionFactory.getCurrentSession().connection())
                def sqlText = ProxyPersonalInformationApi.STORE_PAGE_ACCESS_IN_HISTORY

                logger.debug('sqlText: ' + sqlText)

                sql.call(sqlText, [pidm, p_proxyIDM, p_proxyIDM, pageName])

                logger.debug('finished updateProxyHistoryOnPageAccess')


            } catch (Exception e) {
                logger.error('Problem updateProxyHistoryOnPageAccess')
                logger.error(e)
            }
        }else{
            logger.error('Problem updateProxyHistoryOnPageAccess. Pidm is missing')
        }
    }


    def checkProxyProfileDataOnUpdate(def params) {

        def errorMsgOut = ""

        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        def sqlText = ProxyPersonalInformationApi.CHECK_PROXY_PROFILE_REQUIRED_DATA

        def birthdateString = formatAndValidateBirthdate(params.p_birth_date)

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [p_proxyIDM, params.p_first_name, params.p_mi, params.p_last_name,
                           params.p_surname_prefix, params.p_name_prefix,
                           params.p_name_suffix, params.p_pref_first_name, params.p_email_address, params.p_phone_area,
                           params.p_phone_number, params.p_phone_ext, params.p_ctry_code_phone,
                           params.p_house_number, params.p_street_line1, params.p_street_line2, params.p_street_line3, params.p_street_line4,
                           params.p_city, params.p_stat_code?.code?:"", params.p_zip, params.p_cnty_code?.code?:"", params.p_natn_code?.code?:"",
                           params.p_sex, birthdateString, params.p_ssn, Sql.VARCHAR
        ]){ errorMsg ->

            errorMsgOut = errorMsg ? "ERR_MISSING_DATA:" + errorMsg : errorMsg //The front end will use 'ERR_MISSING_DATA:' to know these are missing data error messages.

            //process i18 error messages proxy.personalinformation.onSave[parameter]
            errorMsg?.split(':')?.each {
                def newMessage = MessageHelper.message('proxy.personalinformation.onSave.' + it.replaceAll("\\s", ""))
                errorMsgOut = errorMsgOut.replace(it, newMessage ? newMessage : it)
            }
        }

        return errorMsgOut
    }


    public def isRequiredDataForProxyProfileComplete(def p_proxyIDM) {

        def errorMsgOut = ""

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        // special OutParameter for cursor type
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR;
            }
        };

        def sqlText = ProxyPersonalInformationApi.PROXY_PERSONAL_INFORMATION

        def proxyProfile = [:]

        sql.call(sqlText, [CURSOR_PARAMETER, p_proxyIDM]) { profile ->
            profile.eachRow() { data ->
                proxyProfile.p_name_prefix = data.GPBPRXY_NAME_PREFIX
                proxyProfile.p_first_name = data.GPBPRXY_FIRST_NAME
                proxyProfile.p_mi = data.GPBPRXY_MI
                proxyProfile.p_surname_prefix = data.GPBPRXY_SURNAME_PREFIX
                proxyProfile.p_last_name = data.GPBPRXY_LAST_NAME
                proxyProfile.p_name_suffix = data.GPBPRXY_NAME_SUFFIX
                proxyProfile.p_pref_first_name = data.GPBPRXY_PREF_FIRST_NAME
                proxyProfile.p_email_address = data.GPBPRXY_EMAIL_ADDRESS
                proxyProfile.p_ctry_code_phone = data.GPBPRXY_CTRY_CODE_PHONE
                proxyProfile.p_phone_area = data.GPBPRXY_PHONE_AREA
                proxyProfile.p_phone_number = data.GPBPRXY_PHONE_NUMBER
                proxyProfile.p_phone_ext = data.GPBPRXY_PHONE_EXT
                proxyProfile.p_house_number = data.GPBPRXY_HOUSE_NUMBER
                proxyProfile.p_street_line1 = data.GPBPRXY_STREET_LINE1
                proxyProfile.p_street_line2 = data.GPBPRXY_STREET_LINE2
                proxyProfile.p_street_line3 = data.GPBPRXY_STREET_LINE3
                proxyProfile.p_street_line4 = data.GPBPRXY_STREET_LINE4
                proxyProfile.p_city = data.GPBPRXY_CITY
                proxyProfile.p_stat_code = State.findByCode(data.GPBPRXY_STAT_CODE) ?: new State()
                proxyProfile.p_zip = data.GPBPRXY_ZIP
                proxyProfile.p_natn_code = Nation.findByCode(data.GPBPRXY_NATN_CODE) ?: new Nation()
                proxyProfile.p_cnty_code = County.findByCode(data.GPBPRXY_CNTY_CODE) ?: new County()
                proxyProfile.p_sex = data.GPBPRXY_SEX
                proxyProfile.p_birth_date = data.GPBPRXY_BIRTH_DATE
                proxyProfile.p_ssn = data.GPBPRXY_SSN
                proxyProfile.p_opt_out_adv_date = (data.GPBPRXY_OPT_OUT_ADV_DATE == null) ? false : true
            }}


        def sqlText1 = ProxyPersonalInformationApi.CHECK_PROXY_PROFILE_REQUIRED_DATA

        DateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy");
        def birthdateString = proxyProfile.p_birth_date ? usFormat.format(proxyProfile.p_birth_date) : null

        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText1, [p_proxyIDM, proxyProfile.p_first_name, proxyProfile.p_mi, proxyProfile.p_last_name,
                            proxyProfile.p_surname_prefix, proxyProfile.p_name_prefix,
                            proxyProfile.p_name_suffix, proxyProfile.p_pref_first_name, proxyProfile.p_email_address, proxyProfile.p_phone_area,
                            proxyProfile.p_phone_number, proxyProfile.p_phone_ext, proxyProfile.p_ctry_code_phone,
                            proxyProfile.p_house_number, proxyProfile.p_street_line1, proxyProfile.p_street_line2, proxyProfile.p_street_line3, proxyProfile.p_street_line4,
                            proxyProfile.p_city, proxyProfile.p_stat_code?.code?:"", proxyProfile.p_zip, proxyProfile.p_cnty_code?.code?:"", proxyProfile.p_natn_code?.code?:"",
                            proxyProfile.p_sex, birthdateString, proxyProfile.p_ssn, Sql.VARCHAR
        ]) { errorMsg ->
            errorMsgOut = errorMsg
        }

        return errorMsgOut

    }

    def getToken(def id){

        def studentToken

        def sqlText = ProxyLandingPageApi.STUDENT_TOKEN

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [id, Sql.VARCHAR
        ]){ token ->
            studentToken = token
        }

        return studentToken
    }

    def getStudentIdFromToken(def token){

        def studentId

        def sqlText = ProxyLandingPageApi.GET_STUDENT_ID_FROM_TOKEN

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [token, Sql.VARCHAR
        ]){ id ->
            studentId = id
        }

        return studentId
    }


    def getStudentListForProxy(def gidm) {

        def studentList = ""

        def sqlText = ProxyLandingPageApi.STUDENT_LIST_FOR_PROXY

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [gidm, Sql.VARCHAR
        ]){ studentListJson ->
            studentList = studentListJson
        }

        def studentsListMap = new JsonSlurper().parseText(studentList)

        studentsListMap <<   getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        studentsListMap.students.active.each { it ->
            def pidm = PersonUtility.getPerson(it.id).pidm
            it.id = getToken(it.id)
            def pages = getProxyPages(gidm, pidm)?.pages
            it.pages = pages
            it.name = PersonUtility.getPreferredNameForProxyDisplay(pidm)
        }

        return studentsListMap
    }


    def getProxyPages(def gidm, def pidm) {

        def  proxyPages = ""

        def sqlText = ProxyLandingPageApi.PROXY_PAGES

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [gidm,pidm,gidm,pidm,pidm,pidm,Sql.VARCHAR
        ]){ proxyPagesJson ->
            proxyPages = proxyPagesJson
        }

        return new JsonSlurper().parseText(proxyPages)
    }


    def getPaymentCenterToken() {

        def gidm = SecurityContextHolder?.context?.authentication?.principal?.gidm

        def connection = new Sql(sessionFactory.getCurrentSession().connection())
        def tokenOut

        Sql sql = new Sql(connection)
        try {
            sql.call("{$Sql.VARCHAR = call gokauth.F_GetProxyAuthToken(${gidm})") { token -> tokenOut = token }
        } catch (e) {
            log.error("ERROR: Could not generate token for the Payment Service. $e")
            throw e
        }

        return tokenOut
    }

    def checkFinaidAccesProxyPages(def page){

        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

        if (p_proxyIDM) {

            def pages = getProxyPages(p_proxyIDM, springSecurityService?.getAuthentication()?.user?.pidm)?.pages

            def foundPage = pages?.find {it.url =~ page}

            if (!foundPage){
                String exceptionMsg = MessageHelper.message("net.hedtech.banner.access.denied.message")
                throw new ApplicationException(this, exceptionMsg);
            }
        }
    }


    /*
     * Private method to convert Date and validate it for birthday parameter
     */
    private String formatAndValidateBirthdate(String bDate) {
        if(bDate) {
            DateFormat javascriptFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = javascriptFormat.parse(bDate)
            String dateString = null
            Calendar tooOldDate = new Date().toCalendar()
            tooOldDate.add(Calendar.YEAR, -150)
            if(tooOldDate.before(date.toCalendar())) {
                SimpleDateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy")
                dateString = usFormat.format(date)
            }
            return dateString
        }
        else {
            return bDate
        }
    }
}
