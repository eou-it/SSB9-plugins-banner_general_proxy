/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.ServiceBase

class ProxyConfigurationService extends ServiceBase {

    static transactional = true

    static final def PAYVEND_TRANS_TIMEOUT = 'PAYVEND_TRANS_TIMEOUT'
    static final def PAYVEND_URL = 'PAYVEND_URL'
    static final def PAYVEND_VENDOR = 'PAYVEND_VENDOR'

    /**
     * Proxy app configuration parameters to retrieve
     */
    def proxyConfigParams = [
            [paramKey: PAYVEND_TRANS_TIMEOUT, defaultValue: '15'],
            [paramKey: PAYVEND_URL],
            [paramKey: PAYVEND_VENDOR]
    ]

    /**
     * Get all configuration params for the Proxy app from Web Tailor.
     * @return Map of all Proxy configuration parameter keys and values
     */
    def getProxyParams () {
        def retParams = [:]
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        // Gather Web Tailor params
        try {
            proxyConfigParams.each {
                def param = getParamFromWebTailor(sql, it)
                retParams[param.key] = param.value
            }
        } finally {
            sql?.close()
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

        try {
            getParamFromWebTailor(sql, requestedParam).value
        } finally {
            sql?.close()
        }
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
}
