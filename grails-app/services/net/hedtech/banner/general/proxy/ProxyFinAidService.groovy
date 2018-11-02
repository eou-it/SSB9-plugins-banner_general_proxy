package net.hedtech.banner.general.proxy

import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.proxy.api.AwardHistoryApi
import net.hedtech.banner.proxy.api.FinAidAwardPackageApi

class ProxyFinAidService {

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

    def fetchAidYearList(int max = 10, int offset = 0, String searchString = '') {
        def resultList = []
        def aidYearSql = """select robinst_aidy_code, robinst_aidy_desc
                            from
                            (select a.*, rownum rnum
                              from
                              (select robinst_aidy_code, robinst_aidy_desc
                                 from robinst
                                 where robinst_info_access_ind = 'Y'
                                 and upper(robinst_aidy_desc) like ?
                                 order by robinst_aidy_start_date desc) a
                              where rownum <= ?)
                            where rnum > ?"""
        String preppedSearchString = '%' + searchString.toUpperCase() + '%'

        resultList = sessionFactory.getCurrentSession().createSQLQuery(aidYearSql)
                .setString(0, preppedSearchString)
                .setInteger(1, max+offset)
                .setInteger(2, offset).list().collect { it = [code: it[0], description: it[1]] }

        resultList
    }

    def getAwardPackage(def pidm, def aidYear) {
        def sqlText = FinAidAwardPackageApi.GET_RORWEBRREC
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def result = [:]
        def rorwebrRec = getFinAidAwardMap(sql, sqlText, pidm, aidYear)

        if (!rorwebrRec.info_access?.equals('Y')) {
            return [hasAwardInfo: false]
        }
        else {
            result.hasAwardInfo = true
            result.aidYearDesc = rorwebrRec.aidYearDesc

            if(rorwebrRec.need_calc_ind.equals('Y')) {
                sqlText = FinAidAwardPackageApi.GET_NEED_CALCULATION
                result.needsCalc = getFinAidAwardMap(sql, sqlText, pidm, aidYear)
            }

            if(rorwebrRec.housing_status_ind.equals('Y')) {
                sqlText = FinAidAwardPackageApi.GET_HOUSING_STATUS
                result.housingStatuses = getFinAidAwardMap(sql, sqlText, pidm, aidYear)
            }

            if(rorwebrRec.enrollment_status.equals('F') || rorwebrRec.enrollment_status.equals('T')) {
                if(rorwebrRec.aid_year >= 2015) {
                    sqlText = FinAidAwardPackageApi.GET_NEW_ENROLLMENT
                }
                else {
                    sqlText = FinAidAwardPackageApi.GET_ENROLLMENT
                }

                String enrollmentJson = ''
                sql.call(sqlText, [ pidm, aidYear, rorwebrRec.enrollment_status, Sql.VARCHAR
                ]){ lv_enroll_json ->
                    enrollmentJson = lv_enroll_json
                }
                def enrollment = new JsonSlurper().parseText(enrollmentJson)
                result.enrollment = enrollment
            }

            if(rorwebrRec.coa_ind.equals('Y')) {
                sqlText = FinAidAwardPackageApi.GET_COST_OF_ATTENDANCE
                result.costOfAttendance = getFinAidAwardMap(sql, sqlText, pidm, aidYear)
            }

            if(rorwebrRec.cum_loan_ind.equals('Y')) {
                sqlText = FinAidAwardPackageApi.GET_CUM_LOAN_INFO
                result.loanInfo = getFinAidAwardMap(sql, sqlText, pidm, aidYear)
            }

            sqlText = FinAidAwardPackageApi.GET_AWARD_INFO
            String awardInfoJson = ''
            String periodInfoJson = ''
            sql.call(sqlText, [ pidm, aidYear, Sql.VARCHAR, Sql.VARCHAR
            ]){ lv_award_json, lv_period_json ->
                awardInfoJson = lv_award_json
                periodInfoJson = lv_period_json
            }
            def awardInfo = new JsonSlurper().parseText(awardInfoJson)
            result.awardInfo = awardInfo

            def periodInfo = new JsonSlurper().parseText(periodInfoJson)
            periodInfo.grandTotal = 0
            def fundTotals = [:]
            periodInfo.periods.each {
                periodInfo.grandTotal += it.total
                it.periodAwards.each {
                    if(fundTotals[it.fundTitle]) {
                        fundTotals[it.fundTitle] += it.amount
                    }
                    else {
                        fundTotals[it.fundTitle] = it.amount
                    }
                }
            }
            periodInfo.fundTotals = fundTotals
            result.periodInfo = periodInfo

            return result
        }
    }

    private getFinAidAwardMap(Sql sql, String sqlText, def pidm, def aidYear) {
        String json = ''
        sql.call(sqlText, [ pidm, aidYear, Sql.VARCHAR
        ]){ lv_json ->
            json = lv_json
        }
        return json ? new JsonSlurper().parseText(json) : null
    }
}
