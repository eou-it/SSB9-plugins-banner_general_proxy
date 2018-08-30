package net.hedtech.banner.general.proxy

import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.proxy.api.AwardHistoryApi

class AwardHistoryProxyService {

    def sessionFactory

    def getAwardHistory(def pidm) {
        def awardHistoryJson = ""
        def sqlText = AwardHistoryApi.AWARD_HISTORY

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call(sqlText, [ pidm, Sql.CLOB
        ]){ lv_awardHistory_json ->
            awardHistoryJson = lv_awardHistory_json ? lv_awardHistory_json.asciiStream.text : ""
        }

        def resultMap = new JsonSlurper().parseText(awardHistoryJson)

        return resultMap
    }
}
