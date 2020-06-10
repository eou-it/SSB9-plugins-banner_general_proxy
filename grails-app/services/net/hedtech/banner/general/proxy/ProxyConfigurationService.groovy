/********************************************************************************
  Copyright 2018-2020 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.configuration.ConfigProperties
import net.hedtech.banner.service.ServiceBase
import grails.gorm.transactions.Transactional
import org.springframework.security.core.context.SecurityContextHolder

@Transactional
class ProxyConfigurationService extends ServiceBase {


    static final def PAYVEND_TRANS_TIMEOUT = 'PAYVEND_TRANS_TIMEOUT'
    static final def PAYVEND_URL = 'PAYVEND_URL'
    static final def PAYVEND_VENDOR = 'PAYVEND_VENDOR'
    def generalSsbProxyService

    /**
     * Proxy app configuration parameters to retrieve
     */
    def proxyConfigParams = [
            [paramKey: PAYVEND_TRANS_TIMEOUT, defaultValue: '15'],
            [paramKey: PAYVEND_URL],
            [paramKey: PAYVEND_VENDOR]
    ]


    /**
     * Get a Proxy Payment Gateway Configuration.
     * @return Map of a Proxy Payment Gateway Configuration Parameters
     */
    def getProxyGatewayParamsForPayment () {
        def proxyPaymentConfigurations = getProxyPaymentConfigurations()
        def enabled = proxyPaymentConfigurations?.get('proxy.payment.gateway.PAYVEND_PROCESS_CENTER_ENABLED')?.toBoolean()
        def authToken = enabled ? generalSsbProxyService.getPaymentCenterToken() : ""

        return [PAYVEND_URL : proxyPaymentConfigurations?.get('proxy.payment.gateway.PAYVEND_URL'),
                PAYVEND_VENDOR : proxyPaymentConfigurations?.get('proxy.payment.gateway.PAYVEND_VENDOR'),
                PAYVEND_PROCESS_CENTER_ENABLED : enabled,
                authToken : authToken
                ]
    }


    /**
     * Get all configuration params for the Proxy app from Web Tailor.
     * @return Map of all Proxy configuration parameter keys and values
     */
    def getProxyParams () {
        def retParams = [:]
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        // Gather Web Tailor params
            proxyConfigParams.each {
                def param = getParamFromWebTailor(sql, it)
                retParams[param.key] = param.value
            }

        retParams
    }

    /**
     * Get the value for the specified Web Tailor parameter key.
     * "defaultValue" can optionally be provided to use as a value in
     * the event the attempt to obtain the parameter from Web Tailor fails.
     * @param key
     * @param defaultValue
     * @return Value for the specified parameter key
     */
    def getParam (key, defaultValue = null) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def requestedParam = [
                paramKey: key,
                defaultValue: defaultValue
        ]
            getParamFromWebTailor(sql, requestedParam).value

    }

    def getParamFromWebTailor (sql, map) {
        def key = map.paramKey
        def defaultVal = map.defaultValue
        def val
        def retMap = [:]

        try {
            sql.call("{? = call twbkwbis.f_fetchwtparam (?)}", [Sql.VARCHAR, key]) { result -> val = result }
        } catch (e) {
            log.error("Error retrieving value for Web Tailor parameter \"" + key + "\". " +
                    "Will attempt to use default value of \"" + defaultVal + "\".", e)

            if (defaultVal) {
                val = defaultVal
            } else {
                log.error("Web Tailor parameter key retrieval failed, and no default value is provided; failed to retrieve value")
                throw new ApplicationException(ProxyConfigurationService, "@@r1:configValueError@@")
            }
        }

        if (!val) {
            if (defaultVal) {
                log.error("No value found for Web Tailor parameter key \"" + key + "\". " +
                        "This should be configured in Web Tailor. Using default value of \"" + defaultVal + "\".")
                val = defaultVal
            } else {
                log.error("No value found for Web Tailor parameter key \"" + key + "\", and default value was not provided." +
                        "This should be configured in Web Tailor.")
                throw new ApplicationException(ProxyConfigurationService, "@@r1:configValueError@@")
            }
        }

        retMap.key = key
        retMap.value = val

        retMap
    }

    /*
        Used instead of Holders as Holders in Student Self-Service does not load General_SS configurations.
    */

    def getProxyPaymentConfigurations() {
        final def SQL_GET_CONFIGURATION_FROM_GUROCFG = """SELECT GENERAL.GUROCFG.GUROCFG_NAME, GENERAL.GUROCFG.GUROCFG_VALUE
        FROM GENERAL.GUROCFG
        WHERE GENERAL.GUROCFG.GUROCFG_GUBAPPL_APP_ID = 'GENERAL_SS'
        AND GENERAL.GUROCFG.GUROCFG_NAME            IN ('proxy.payment.gateway.PAYVEND_PROCESS_CENTER_ENABLED','proxy.payment.gateway.PAYVEND_URL','proxy.payment.gateway.PAYVEND_VENDOR')"""
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def results = sql.rows(SQL_GET_CONFIGURATION_FROM_GUROCFG)

        def configurationMap = [:]
        results.each {
            configurationMap.put(it.GUROCFG_NAME, it.GUROCFG_VALUE?.characterStream?.text)
        }
        configurationMap
    }

    public static void injectHoldersWithProxyConfigurations() {
        def proxyConfigurations = ConfigProperties.fetchByAppId('BAN9_PROXY')
        proxyConfigurations.each { configuration ->
            Holders.config.setAt(configuration.configName, configuration.configValue)
        }
    }

    def getFinaidConfigurationsBasedOnRole(){
        def finaidConfigurations = [:]
        def proxyConfigurations = ConfigProperties.fetchByAppId('BAN9_PROXY')

        /*These configurations are retrieved from the database as strings.
        The JsonSlurper will convert them to Arraylists.*/
        def jsonSlurper = new JsonSlurper()

        def roles = getRoles()

        proxyConfigurations.each { configuration ->
            if (configuration.configName.contains("financialAid")){
                def value = finaidConfigContainsUserRole(roles, jsonSlurper.parseText(configuration.configValue)) ? "Y" : "N"
                finaidConfigurations << [(configuration.configName.substring(13)) : value]
            }
        }
        finaidConfigurations
    }

    private finaidConfigContainsUserRole(roles, finaidConfig) {
        def found = false
        finaidConfig.each {
            if (roles."${it}") {
                found = true
                return found
            }
        }
        return found
    }

    private getRoles() {
        [
                STUDENT : hasUserRole("STUDENT"),
                EMPLOYEE: hasUserRole("EMPLOYEE"),
                FACULTY : hasUserRole("FACULTY"),
                PROXY   : SecurityContextHolder?.context?.authentication?.principal?.gidm ? true : false
        ]
    }

    private hasUserRole(String role) {
        try {
            def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities
            return authorities.any { it.getAssignedSelfServiceRole().contains(role) }
        } catch (MissingPropertyException it) {
            log.error("principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw new ApplicationException('UserRoleService', it)
        }
    }
}
