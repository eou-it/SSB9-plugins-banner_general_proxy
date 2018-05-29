package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SC
import org.springframework.web.context.request.RequestContextHolder

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

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y"), gidm : gidm]
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

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y"), gidm : gidm]
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
                           p_proxyIDM , Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR])
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

}
