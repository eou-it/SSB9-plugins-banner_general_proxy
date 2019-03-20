/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.student.history

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.query.DynamicFinder
import org.apache.commons.lang.StringUtils

import javax.persistence.*

/**
 * View used to display list of course grades of a student
 */
@Entity
@Table(name = "SVQ_COURSE_DETAIL")
@NamedQueries(value = [
        @NamedQuery(name = "HistoryStudentCourseDetail.fetchAllLevelsByPidmTermLikeAndLevelLike",
                query = """SELECT a.levelCode,a.levelDescription
           FROM  HistoryStudentCourseDetail a
	  	   WHERE a.studentPidm = :pidm
	  	   AND a.termCode like :termFilter
	  	   AND (a.levelCode LIKE  :levelFilter OR a.upperCaseLevelDescription LIKE :levelFilter)
	  	   GROUP BY a.levelCode,a.levelDescription
	  	   ORDER BY a.levelCode,a.levelDescription"""),
        @NamedQuery(name = "HistoryStudentCourseDetail.fetchAllStudyPathsByPidmTermLikeLevelLikeAndStudyPathLike",
                query = """SELECT a.studyPath,a.studyPathName
           FROM  HistoryStudentCourseDetail a
	  	   WHERE a.studentPidm = :pidm
	  	   AND a.termCode like :termFilter
	  	   AND a.levelCode like :levelFilter
	  	   AND (a.studyPath LIKE  :studyPathFilter OR a.upperCaseStudyPathName LIKE :studyPathFilter)
	  	   AND a.studyPath != '-1'
	  	   GROUP BY a.studyPath,a.studyPathName
	  	   ORDER BY a.studyPath,a.studyPathName""")
])
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
class HistoryStudentCourseDetail {

    @Id
    @Column(name = "ROW_ID")
    String id

    @Version
    @Column(name = "VERSION")
    Long version

    @Column(name = "STUDENT_PIDM")
    Integer studentPidm

    @Column(name = "TERM_CODE")
    String termCode

    @Column(name = "TERM_DESC")
    String termDescription

    @Column(name = "CRN")
    String courseReferenceNumber

    @Column(name = "SUBJECT_CODE")
    String subjectCode

    @Column(name = "COURSE_NUMBER")
    String courseNumber

    @Column(name = "COURSE_TITLE")
    String courseTitle

    @Column(name = "UPPERCASE_COURSE_TITLE")
    String upperCaseCourseTitle

    @Column(name = "SECTION")
    String section

    @Column(name = "FINAL_GRADE")
    String finalGrade

    @Column(name = "MIDTERM_GRADE")
    String midtermGrade

    @Column(name = "HISTORY_FINAL_GRADE")
    String historyFinalGrade

    @Column(name = "CALCULATED_FINAL_GRADE")
    String calculatedFinalGrade

    @Column(name = "GRADE_DETAIL_DISPLAY_IND")
    String gradeDetailDisplayInd

    @Column(name = "GRADE_CODE_INCMP_FINAL")
    String gradeCodeIncmpFinal

    @Column(name = "INCOMPLETE_EXT_DATE")
    Date incmpExtnDate

    @Column(name = "GRADE_DATE")
    Date gradeDate

    @Column(name = "CAMPUS_CODE")
    String campusCode

    @Column(name = "EARN_HOURS")
    BigDecimal hoursEarned

    @Column(name = "CREDIT_HOURS")
    BigDecimal hoursAttempted

    @Column(name = "GPA_HOURS")
    BigDecimal gpaHours

    @Column(name = "QUALITY_POINTS")
    BigDecimal qualityPoints

    @Column(name = "STSP_KEY_SEQUENCE")
    String studyPath

    @Column(name = "LEVEL_CODE")
    String levelCode

    @Column(name = "LEVEL_DESC")
    String levelDescription

    @Column(name = "UPPERCASE_LEVEL_DESC")
    String upperCaseLevelDescription

    @Column(name = "HAS_COMPONENT")
    String hasComponent

    @Column(name = "STUDYPATH_NAME")
    String studyPathName

    @Column(name = "UPPERCASE_STUDYPATH_NAME")
    String upperCaseStudyPathName


    def static countAllByPidmTermLevelAndStudyPath(filterData) {
        if (!filterData.params.filterText) {
            filterData.params.put('filterText', '%')
        }
        filterData.params.filterText = filterData.params.filterText?.toUpperCase()
        finderByPidmTermLevelAndStudyPath().count(filterData)
    }


    def static fetchSearchByPidmTermLevelAndStudyPath(filterData, pagingAndSortParams) {
        if (!filterData.params.filterText) {
            filterData.params.put('filterText', '%')
        }
        filterData.params.filterText = filterData.params.filterText?.toUpperCase()
        finderByPidmTermLevelAndStudyPath().find(filterData, pagingAndSortParams)
    }


    def private static finderByPidmTermLevelAndStudyPath = {
        def query = """FROM  HistoryStudentCourseDetail a
        WHERE  a.studentPidm = :studentPidm
        AND a.termCode like :termCode
        AND a.levelCode like :levelCode
        AND a.studyPath like :studyPath
        AND (a.upperCaseCourseTitle like :filterText OR a.subjectCode like :filterText)"""
        return new DynamicFinder(HistoryStudentCourseDetail.class, query, "a")
    }


    def static fetchSearchByPidmTermLevelStudyPathAndSubjectCourseFilter(filterData, pagingAndSortParams) {
        if (!filterData.params.filterText) {
            filterData.params.put('filterText', '%')
        }
        filterData.params.filterText = filterData.params.filterText?.toUpperCase()
        finderByPidmTermLevelStudyPathAndSubjectCourseFilter().find(filterData, pagingAndSortParams)
    }

    def private static finderByPidmTermLevelStudyPathAndSubjectCourseFilter = {
        def query = """FROM  HistoryStudentCourseDetail a
        WHERE  a.studentPidm = :studentPidm
        AND a.termCode like :termCode
        AND a.levelCode like :levelCode
        AND a.studyPath like :studyPath
        AND a.hasComponent = 'Y' AND gradeDetailDisplayInd = 'Y'
        AND (a.subjectCode like :filterText OR a.courseNumber like :filterText OR a.courseReferenceNumber like :filterText) """
        return new DynamicFinder(HistoryStudentCourseDetail.class, query, "a")
    }


    static List fetchAllLevelsByPidmTermLikeAndLevelLike(Integer pidm, String termFilter, String levelFilter) {
        List historyStudentCourseDetails = []
        if (null != pidm) {
            HistoryStudentCourseDetail.withSession { session ->
                historyStudentCourseDetails = session.getNamedQuery('HistoryStudentCourseDetail.fetchAllLevelsByPidmTermLikeAndLevelLike')
                        .setInteger('pidm', pidm)
                        .setString('termFilter', prepareParams(termFilter))
                        .setString('levelFilter', prepareParams(levelFilter)?.toUpperCase()).list()
                historyStudentCourseDetails = historyStudentCourseDetails.collect {
                    [code: it[0], description: it[1]]
                }
            }
        }
        return historyStudentCourseDetails
    }


    static List fetchAllStudyPathsByPidmTermLikeLevelLikeAndStudyPathLike(Integer pidm, String termFilter, String levelFilter, String studyPathFilter) {
        List historyStudentStudypaths = []
        if (null != pidm) {
            HistoryStudentCourseDetail.withSession { session ->
                historyStudentStudypaths = session.getNamedQuery('HistoryStudentCourseDetail.fetchAllStudyPathsByPidmTermLikeLevelLikeAndStudyPathLike')
                        .setInteger('pidm', pidm)
                        .setString('termFilter', prepareParams(termFilter))
                        .setString('levelFilter', prepareParams(levelFilter))
                        .setString('studyPathFilter', prepareParams(studyPathFilter)?.toUpperCase()).list()
                historyStudentStudypaths = historyStudentStudypaths.collect {
                    [code: it[0], description: it[1]]
                }
            }
        }
        return historyStudentStudypaths
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
