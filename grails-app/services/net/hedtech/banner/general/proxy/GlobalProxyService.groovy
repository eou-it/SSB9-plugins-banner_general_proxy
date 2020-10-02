/********************************************************************************
  Copyright 2020 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.proxy.api.GlobalProxyManagementApi
import net.hedtech.banner.proxy.api.ProxyManagementApi

@Transactional
class GlobalProxyService {
    def personEmailService
    def sessionFactory                     // injected by Spring
    def dataSource

    def doesUserHaveActivePreferredEmail(pidm) {
        def userPreferredEmail = personEmailService.findPreferredEmailAddress(pidm)
        def doesUserHaveActivePreferredEmail = userPreferredEmail != null
        doesUserHaveActivePreferredEmail
    }

    def getRelationshipOptions(pidm) {
        def relationships
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def sqlText = GlobalProxyManagementApi.RELATIONSHIP_OPTION_LIST

        sql.call(sqlText, [pidm, Sql.VARCHAR])
                { relationshipsJson ->
                    relationships = relationshipsJson
                }

        relationships ? new JsonSlurper().parseText(relationships) : [:]
    }

    def getDataModelOnRelationshipChange(def params){
        def data = [:]
        data.pages = getProxyPages(params)
        data
    }

    def getProxyPages(def params) {

        def  proxyPages = ""

        def sqlText = GlobalProxyManagementApi.GLOBAL_PROXY_PAGES

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [params.p_retp_code, Sql.VARCHAR]){
            proxyPagesJson ->
            proxyPages = proxyPagesJson
        }

        def authPages = new JsonSlurper().parseText(proxyPages)
        authPages?.pages?.each{
            it?.auth = (it.auth == "Y")? true : false
        }

        return authPages
    }

}
