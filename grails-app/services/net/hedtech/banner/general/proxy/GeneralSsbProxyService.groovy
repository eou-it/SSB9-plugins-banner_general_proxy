package net.hedtech.banner.general.proxy

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
        sql.call(sqlText, [token, Sql.VARCHAR, Sql.NUMERIC, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
                { loginOut, gidmOut, actionVerifyOut, pinOut, msgOut, errorOut ->
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
                proxyProfile.p_phone_area = data.GPBPRXY_PHONE_AREA
                proxyProfile.p_phone_number = data.GPBPRXY_PHONE_NUMBER
                proxyProfile.p_phone_ext = data.GPBPRXY_PHONE_EXT
                proxyProfile.p_street_line1 = data.GPBPRXY_STREET_LINE1
                proxyProfile.p_street_line2 = data.GPBPRXY_STREET_LINE2
                proxyProfile.p_street_line3 = data.GPBPRXY_STREET_LINE3
                proxyProfile.p_city = data.GPBPRXY_CITY
                proxyProfile.p_stat_code = data.GPBPRXY_STAT_CODE
                proxyProfile.p_zip = data.GPBPRXY_ZIP
                proxyProfile.p_natn_code = data.GPBPRXY_NATN_CODE
                proxyProfile.p_sex = data.GPBPRXY_SEX
                proxyProfile.p_birth_date = df.format(data.GPBPRXY_BIRTH_DATE)
                proxyProfile.p_ssn = data.GPBPRXY_SSN
            }

        }

        println "proxyProfile: " + proxyProfile

        return proxyProfile
    }


    def updateProxyProfile(def params) {

        println "Update Proxy Profile: " + params
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

        def sql = new Sql(sessionFactory.getCurrentSession().connection())


        def sqlText = sqlFileLoadService.getSqlTextMap().updateProfile?.sqlText

        def bDate = Date.parse('yyyy-MM-dd', params.p_birth_date).format('MM/dd/yyyy')

        sql.call(sqlText, [p_proxyIDM, p_proxyIDM, params.p_first_name, params.p_last_name,
                           p_proxyIDM , params.p_mi, params.p_surname_prefix, params.p_name_prefix,
                           params.p_name_suffix, params.p_pref_first_name, params.p_phone_area,
                           params.p_phone_number, params.p_phone_ext, params.p_ctry_code_phone,
                           params.p_house_number, params.p_street_line1, params.p_street_line2, params.p_street_line3, params.p_street_line4,
                           params.p_city, params.p_stat_code, params.p_zip, params.p_cnty_code, params.p_natn_code,
                           params.p_sex, bDate, params.p_ssn
                          ])

    }
}
