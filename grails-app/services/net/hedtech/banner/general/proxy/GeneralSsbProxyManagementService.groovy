/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.proxy.api.ProxyManagementApi
import org.springframework.security.core.context.SecurityContextHolder

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

}
