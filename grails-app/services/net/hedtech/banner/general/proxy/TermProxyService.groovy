/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.proxy.student.history.HistoryTermForStudentGradesProxy

class TermProxyService {

    def proxyStudentService

    def fetchTermList(def pidm, def searchString, def max, def offset) {
        if (!proxyStudentService.checkIfStudentInstalled()) {
            return []
        }
        else {
            def terms = HistoryTermForStudentGradesProxy.fetchAllTermsByStudentPidmAndTerm(pidm, searchString, max, offset)
            return terms
        }
    }
}
