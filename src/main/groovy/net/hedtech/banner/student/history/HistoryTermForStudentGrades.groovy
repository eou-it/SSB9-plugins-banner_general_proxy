/*
 * ********************************************************
 *    Copyright 2018 Ellucian Company L.P. and its affiliates.
 * ********************************************************
 */
package net.hedtech.banner.student.history

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.commons.lang.StringUtils

import javax.persistence.*

/**
 * View used to display terms for students on the Student Grades page.
 */
@Entity
@Table(name = "SVQ_TERM_STUDENT_GRADES")
@NamedQueries(value = [
        @NamedQuery(name = "HistoryTermForStudentGrades.fetchAllTermsByStudentPidmAndTerm",
                query = """SELECT a.code,a.description
                            FROM HistoryTermForStudentGrades a
                            WHERE a.studentPidm = :studentPidm
                            AND (UPPER(a.code) LIKE  UPPER(:filter) OR UPPER(a.searchDescription) LIKE UPPER(:filter))
                            GROUP BY a.code,a.description
                            ORDER BY a.code desc, a.description"""),
        @NamedQuery(name = "HistoryTermForStudentGrades.fetchAllTermsForFacultyByStudentPidmAndTerm",
                query = """SELECT a.code,a.descriptionForFaculty
                            FROM HistoryTermForStudentGrades a
                            WHERE a.studentPidm = :studentPidm
                            AND (UPPER(a.code) LIKE  UPPER(:filter) OR UPPER(a.searchDescription) LIKE UPPER(:filter))
                            GROUP BY a.code,a.descriptionForFaculty
                            ORDER BY a.code desc, a.descriptionForFaculty""")
])
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
class HistoryTermForStudentGrades {

    @Id
    @Column(name = "STVTERM_SURROGATE_ID")
    Long id

    @Version
    @Column(name = "STVTERM_VERSION")
    Long version

    @Column(name = "STVTERM_CODE")
    String code

    @Column(name = "STVTERM_DESC")
    String searchDescription

    @Column(name = "STVTERM_DATE_DESC")
    String description

    @Column(name = "STVTERM_DATE_DESC_FAC")
    String descriptionForFaculty

    @Column(name = "STUDENT_PIDM")
    Integer studentPidm

    @Column(name = "COURSE_REFERENCE_NUMBER")
    String courseReferenceNumber


    static List fetchAllTermsByStudentPidmAndTerm(Integer studentPidm, String termFilter, Integer max = 0, Integer offset = 0) {
        List result = []
        if (null != studentPidm) {
            HistoryTermForStudentGrades.withSession { session ->
                def query = session.getNamedQuery('HistoryTermForStudentGrades.fetchAllTermsByStudentPidmAndTerm')
                query.setInteger('studentPidm', studentPidm)
                query.setString('filter', prepareParams(termFilter))
                if (max) {
                    query.setMaxResults(max)
                }
                if (offset) {
                    query.setFirstResult(offset)
                }
                result = query.list().collect {
                    [code: it[0], description: it[1]]
                }
            }
        }
        return result
    }


    static List fetchAllTermsForFacultyByStudentPidmAndTerm(Integer studentPidm, String termFilter, Integer max = 0, Integer offset = 0) {
        List result = []
        if (null != studentPidm) {
            HistoryTermForStudentGrades.withSession { session ->
                def query = session.getNamedQuery('HistoryTermForStudentGrades.fetchAllTermsForFacultyByStudentPidmAndTerm')
                query.setInteger('studentPidm', studentPidm)
                query.setString('filter', prepareParams(termFilter))
                if (max) {
                    query.setMaxResults(max)
                }
                if (offset) {
                    query.setFirstResult(offset)
                }
                result = query.list().collect {
                    [code: it[0], description: it[1]]
                }
            }
        }
        return result
    }


    private static String prepareParams(value) {
        if (StringUtils.isBlank(value)) {
            value = "%"
        } else if (!(value =~ /%/)) {
            value = "%" + value + "%"
        }
        return value
    }
}
