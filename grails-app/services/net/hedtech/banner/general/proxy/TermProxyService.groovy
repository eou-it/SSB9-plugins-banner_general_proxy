package net.hedtech.banner.general.proxy

import net.hedtech.banner.student.history.HistoryTermForStudentGrades

class TermProxyService {

    def fetchTermList(def pidm, def searchString) {
        def  terms = HistoryTermForStudentGrades.fetchAllTermsByStudentPidmAndTerm(pidm, searchString)
        return terms
    }
}
