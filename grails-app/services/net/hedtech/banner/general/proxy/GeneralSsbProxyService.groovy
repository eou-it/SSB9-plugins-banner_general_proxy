package net.hedtech.banner.general.proxy

import groovy.json.JsonSlurper
import groovy.sql.OutParameter
import groovy.sql.Sql
import net.sf.json.JSON
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import groovy.json.JsonBuilder
import oracle.jdbc.driver.OracleTypes
import groovy.sql.OutParameter

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.hedtech.banner.exceptions.ApplicationException

class GeneralSsbProxyService {
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def grailsApplication                  // injected by Spring
    def sqlFileLoadService


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

        def sqlText = sqlFileLoadService.getSqlTextMap().setProxy?.sqlText
        sql.call(sqlText, [token, Sql.NUMERIC, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { gidmOut, actionVerifyOut, pinOut, msgOut, loginOut, errorOut  ->
                    login = loginOut
                    gidm = gidmOut
                    actionVerify = actionVerifyOut
                    doPin = pinOut
                    msg = msgOut
                    error = errorOut
                }


        println "GIDM: " + gidm
        println "ActionVerify: " + actionVerify
        println "LoginOUT: " + login
        println "Do Pin: " + doPin
        println "Message: " + msg
        println "Error: " + error

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

        def sqlText = sqlFileLoadService.getSqlTextMap().setProxyVerify?.sqlText
        sql.call(sqlText, [token, Sql.VARCHAR, verify, Sql.NUMERIC, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { loginOut, gidmOut, actionVerifyOut, pinOut, msgOut, errorOut ->
                    login = loginOut
                    //gidm = gidmOut
                    actionVerify = actionVerifyOut
                    doPin = pinOut
                    msg = msgOut
                    error = errorOut
                }

        println "GIDM: " + gidm
        println "ActionVerify: " + actionVerify
        println "LoginOUT: " + login
        println "Do Pin: " + doPin
        println "Message: " + msg
        println "Error: " + error

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y"), gidm: gidm]
    }


    public def savePin(def p_proxyIDM, def p_pin1, def p_pin2, def p_email, def p_pin_orig) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def msg
        def error
        def errorStatus

        def sqlText = sqlFileLoadService.getSqlTextMap().savePin?.sqlText
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

        println "errorStatus: " + errorStatus
        println "msg: " + msg
        println "error: " + error

        return [errorStatus: errorStatus.equals("Y"), message: msg, error: error]

    }


    public def getPersonalInformation(def gidm) {

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        // special OutParameter for cursor type
        OutParameter CURSOR_PARAMETER = new OutParameter() {
            public int getType() {
                return OracleTypes.CURSOR;
            }
        };

        def sqlText = sqlFileLoadService.getSqlTextMap().getProxyPersonalInformation?.sqlText


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

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
                proxyProfile.p_stat_code = data.GPBPRXY_STAT_CODE
                proxyProfile.p_zip = data.GPBPRXY_ZIP
                proxyProfile.p_natn_code = data.GPBPRXY_NATN_CODE
                proxyProfile.p_cnty_code = data.GPBPRXY_CNTY_CODE
                proxyProfile.p_sex = data.GPBPRXY_SEX
                proxyProfile.p_birth_date = (data.GPBPRXY_BIRTH_DATE==null) ? "" : df.format(data.GPBPRXY_BIRTH_DATE)
                proxyProfile.p_ssn = data.GPBPRXY_SSN
            }

        }

        def proxyUiRules = [:]
        sqlText = sqlFileLoadService.getSqlTextMap().getProxyProfileUiRules?.sqlText

        sql.call(sqlText, [gidm, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR,
                           Sql.VARCHAR, Sql.VARCHAR ])
                { show_p_name_prefix, show_p_mi, show_p_surname_prefix,
                  show_p_name_suffix, show_p_pref_first_name, show_p_phone_area,
                  show_p_phone_number, show_p_phone_ext, show_p_ctry_code_phone,
                  show_p_house_number, show_p_street_line1,show_p_street_line2,show_p_street_line3,show_p_street_line4,
                  show_p_city, show_p_stat_code, show_p_zip, show_p_cnty_code, show_p_natn_code,
                  show_p_sex, show_p_birth_date, show_p_ssn ->

                    proxyUiRules."p_name_prefix" = [fieldLength: 20]
                    if (show_p_name_prefix.equals("V")){
                        proxyUiRules."p_name_prefix".putAll([visible: true, required : false])
                    }else if(show_p_name_prefix.equals("N")){
                        proxyUiRules."p_name_prefix".putAll([visible: false, required : false])
                    }else if(show_p_name_prefix.equals("Y")){
                        proxyUiRules."p_name_prefix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_first_name" = [fieldLength: 60]

                    proxyUiRules."p_mi" = [fieldLength: 60]
                    if (show_p_mi.equals("V")){
                        proxyUiRules."p_mi".putAll([visible: true, required : false])
                    }else if(show_p_mi.equals("N")){
                        proxyUiRules."p_mi".putAll([visible: false, required : false])
                    }else if(show_p_mi.equals("Y")){
                        proxyUiRules."p_mi".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_last_name" = [fieldLength: 60]

                    proxyUiRules."p_surname_prefix" = [fieldLength: 60]
                    if (show_p_surname_prefix.equals("V")){
                        proxyUiRules."p_surname_prefix".putAll([visible: true, required : false])
                    }else if(show_p_surname_prefix.equals("N")){
                        proxyUiRules."p_surname_prefix".putAll([visible: false, required : false])
                    }else if(show_p_surname_prefix.equals("Y")){
                        proxyUiRules."p_surname_prefix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_name_suffix" = [fieldLength: 20]
                    if (show_p_name_suffix.equals("V")){
                        proxyUiRules."p_name_suffix".putAll([visible: true, required : false])
                    }else if(show_p_name_suffix.equals("N")){
                        proxyUiRules."p_name_suffix".putAll([visible: false, required : false])
                    }else if(show_p_name_suffix.equals("Y")){
                        proxyUiRules."p_name_suffix".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_pref_first_name" = [fieldLength: 60]
                    if (show_p_pref_first_name.equals("V")){
                        proxyUiRules."p_pref_first_name".putAll([visible: true, required : false])
                    }else if(show_p_pref_first_name.equals("N")){
                        proxyUiRules."p_pref_first_name".putAll([visible: false, required : false])
                    }else if(show_p_pref_first_name.equals("Y")){
                        proxyUiRules."p_pref_first_name".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_email_address" = [fieldLength: 75]

                    proxyUiRules."p_phone_area" = [fieldLength: 6]
                    if (show_p_phone_area.equals("V")){
                        proxyUiRules."p_phone_area".putAll([visible: true, required : false])
                    }else if(show_p_phone_area.equals("N")){
                        proxyUiRules."p_phone_area".putAll([visible: false, required : false])
                    }else if(show_p_phone_area.equals("Y")){
                        proxyUiRules."p_phone_area".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_phone_number" = [fieldLength: 12]
                    if (show_p_phone_number.equals("V")){
                        proxyUiRules."p_phone_number".putAll([visible: true, required : false])
                    }else if(show_p_phone_number.equals("N")){
                        proxyUiRules."p_phone_number".putAll([visible: false, required : false])
                    }else if(show_p_phone_number.equals("Y")){
                        proxyUiRules."p_phone_number".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_phone_ext" = [fieldLength: 10]
                    if (show_p_phone_ext.equals("V")){
                        proxyUiRules."p_phone_ext".putAll([visible: true, required : false])
                    }else if(show_p_phone_ext.equals("N")){
                        proxyUiRules."p_phone_ext".putAll([visible: false, required : false])
                    }else if(show_p_phone_ext.equals("Y")){
                        proxyUiRules."p_phone_ext".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_ctry_code_phone" = [fieldLength: 4]
                    if (show_p_ctry_code_phone.equals("V")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: true, required : false])
                    }else if(show_p_ctry_code_phone.equals("N")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: false, required : false])
                    }else if(show_p_ctry_code_phone.equals("Y")){
                        proxyUiRules."p_ctry_code_phone".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_house_number" = [fieldLength: 10]
                    if (show_p_house_number.equals("V")){
                        proxyUiRules."p_house_number".putAll([visible: true, required : false])
                    }else if(show_p_house_number.equals("N")){
                        proxyUiRules."p_house_number".putAll([visible: false, required : false])
                    }else if(show_p_house_number.equals("Y")){
                        proxyUiRules."p_house_number".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line1" = [fieldLength: 75]
                    if (show_p_street_line1.equals("V")){
                        proxyUiRules."p_street_line1".putAll([visible: true, required : false])
                    }else if(show_p_street_line1.equals("N")){
                        proxyUiRules."p_street_line1".putAll([visible: false, required : false])
                    }else if(show_p_street_line1.equals("Y")){
                        proxyUiRules."p_street_line1".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line2" = [fieldLength: 75]
                    if (show_p_street_line2.equals("V")){
                        proxyUiRules."p_street_line2".putAll([visible: true, required : false])
                    }else if(show_p_street_line2.equals("N")){
                        proxyUiRules."p_street_line2".putAll([visible: false, required : false])
                    }else if(show_p_street_line2.equals("Y")){
                        proxyUiRules."p_street_line2".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line3" = [fieldLength: 75]
                    if (show_p_street_line3.equals("V")){
                        proxyUiRules."p_street_line3".putAll([visible: true, required : false])
                    }else if(show_p_street_line3.equals("N")){
                        proxyUiRules."p_street_line3".putAll([visible: false, required : false])
                    }else if(show_p_street_line3.equals("Y")){
                        proxyUiRules."p_street_line3".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_street_line4" = [fieldLength: 75]
                    if (show_p_street_line4.equals("V")){
                        proxyUiRules."p_street_line4".putAll([visible: true, required : false])
                    }else if(show_p_street_line1.equals("N")){
                        proxyUiRules."p_street_line4".putAll([visible: false, required : false])
                    }else if(show_p_street_line4.equals("Y")){
                        proxyUiRules."p_street_line4".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_city" = [fieldLength: 50]
                    if (show_p_city.equals("V")){
                        proxyUiRules."p_city".putAll([visible: true, required : false])
                    }else if(show_p_city.equals("N")){
                        proxyUiRules."p_city".putAll([visible: false, required : false])
                    }else if(show_p_city.equals("Y")){
                        proxyUiRules."p_city".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_stat_code" = [fieldLength: 3]
                    if (show_p_stat_code.equals("V")){
                        proxyUiRules."p_stat_code".putAll([visible: true, required : false])
                    }else if(show_p_stat_code.equals("N")){
                        proxyUiRules."p_stat_code".putAll([visible: false, required : false])
                    }else if(show_p_stat_code.equals("Y")){
                        proxyUiRules."p_stat_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_zip" = [fieldLength: 30]
                    if (show_p_zip.equals("V")){
                        proxyUiRules."p_zip".putAll([visible: true, required : false])
                    }else if(show_p_zip.equals("N")){
                        proxyUiRules."p_zip".putAll([visible: false, required : false])
                    }else if(show_p_zip.equals("Y")){
                        proxyUiRules."p_zip".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_cnty_code" = [fieldLength: 5]
                    if (show_p_cnty_code.equals("V")){
                        proxyUiRules."p_cnty_code".putAll([visible: true, required : false])
                    }else if(show_p_cnty_code.equals("N")){
                        proxyUiRules."p_cnty_code".putAll([visible: false, required : false])
                    }else if(show_p_cnty_code.equals("Y")){
                        proxyUiRules."p_cnty_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_natn_code" = [fieldLength: 5]
                    if (show_p_natn_code.equals("V")){
                        proxyUiRules."p_natn_code".putAll([visible: true, required : false])
                    }else if(show_p_natn_code.equals("N")){
                        proxyUiRules."p_natn_code".putAll([visible: false, required : false])
                    }else if(show_p_natn_code.equals("Y")){
                        proxyUiRules."p_natn_code".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_sex" = [fieldLength: 1]
                    if (show_p_sex.equals("V")){
                        proxyUiRules."p_sex".putAll([visible: true, required : false])
                    }else if(show_p_sex.equals("N")){
                        proxyUiRules."p_sex".putAll([visible: false, required : false])
                    }else if(show_p_sex.equals("Y")){
                        proxyUiRules."p_sex".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_ssn" = [fieldLength: 9]
                    if (show_p_ssn.equals("V")){
                        proxyUiRules."p_ssn".putAll([visible: true, required : false])
                    }else if(show_p_ssn.equals("N")){
                        proxyUiRules."p_ssn".putAll([visible: false, required : false])
                    }else if(show_p_ssn.equals("Y")){
                        proxyUiRules."p_ssn".putAll([visible: true, required : true])
                    }

                    proxyUiRules."p_birth_date" = [fieldLength: 20]
                    if (show_p_birth_date.equals("V")){
                        proxyUiRules."p_birth_date".putAll([visible: true, required : false])
                    }else if(show_p_birth_date.equals("N")){
                        proxyUiRules."p_birth_date".putAll([visible: false, required : false])
                    }else if(show_p_birth_date.equals("Y")){
                        proxyUiRules."p_birth_date".putAll([visible: true, required : true])
                    }

                }

        return [proxyProfile : proxyProfile, proxyUiRules : proxyUiRules]
    }


    def updateProxyProfile(def params) {

        //println "Update Proxy Profile: " + params

        def updateRulesErrors = checkProxyProfileDataOnUpdate(params)

        if (updateRulesErrors){
            throw new ApplicationException(GeneralSsbProxyService, updateRulesErrors)
        }
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())


        def sqlText = sqlFileLoadService.getSqlTextMap().updateProfile?.sqlText

        def bDate = params.p_birth_date ? Date.parse('yyyy-MM-dd', params.p_birth_date).format('MM/dd/yyyy') : ""

        sql.call(sqlText, [p_proxyIDM, p_proxyIDM, params.p_first_name, params.p_last_name,
                           p_proxyIDM , params.p_mi, params.p_surname_prefix, params.p_name_prefix,
                           params.p_name_suffix, params.p_pref_first_name, params.p_phone_area,
                           params.p_phone_number, params.p_phone_ext, params.p_ctry_code_phone,
                           params.p_house_number, params.p_street_line1, params.p_street_line2, params.p_street_line3, params.p_street_line4,
                           params.p_city, params.p_stat_code, params.p_zip, params.p_cnty_code, params.p_natn_code,
                           params.p_sex, bDate, params.p_ssn, p_proxyIDM
                          ])

    }



    def updateProxyHistoryOnLogin() {
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = sqlFileLoadService.getSqlTextMap().storeLoginInHistory?.sqlText

        sql.call(sqlText,
                [p_proxyIDM, p_proxyIDM, p_proxyIDM
        ])

    }


    def checkProxyProfileDataOnUpdate(def params) {

        def errorMsgOut = ""

        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        def sqlText = sqlFileLoadService.getSqlTextMap().checkProxyProfileRequiredData?.sqlText

        def bDate = params.p_birth_date ? Date.parse('yyyy-MM-dd', params.p_birth_date).format('MM/dd/yyyy') : ""

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [p_proxyIDM, params.p_first_name, params.p_mi, params.p_last_name,
                           params.p_surname_prefix, params.p_name_prefix,
                           params.p_name_suffix, params.p_pref_first_name, params.p_email_address, params.p_phone_area,
                           params.p_phone_number, params.p_phone_ext, params.p_ctry_code_phone,
                           params.p_house_number, params.p_street_line1, params.p_street_line2, params.p_street_line3, params.p_street_line4,
                           params.p_city, params.p_stat_code, params.p_zip, params.p_cnty_code, params.p_natn_code,
                           params.p_sex, bDate, params.p_ssn, Sql.VARCHAR
        ]){ errorMsg ->
            errorMsgOut = errorMsg
        }

        return errorMsgOut
    }


    def getStudentListForProxy(def gidm) {

        def studentList = ""

        def sqlText = sqlFileLoadService.getSqlTextMap().getStudentListForProxy?.sqlText

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [gidm, Sql.VARCHAR
        ]){ studentListJson ->
            studentList = studentListJson
        }

        return new JsonSlurper().parseText(studentList)
    }

}
