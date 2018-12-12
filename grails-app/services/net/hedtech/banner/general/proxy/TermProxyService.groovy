/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.student.history.HistoryTermForStudentGrades

class TermProxyService {

    def proxyStudentService

    def fetchTermList(def pidm, def searchString, def max, def offset) {
        if (!proxyStudentService.checkIfStudentInstalled()) {
            return []
        }
        else {
            def terms = HistoryTermForStudentGrades.fetchAllTermsByStudentPidmAndTerm(pidm, searchString, max, offset)
            return terms
        }
    }
}
