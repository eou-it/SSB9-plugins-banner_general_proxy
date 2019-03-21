/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import net.hedtech.banner.proxy.api.ViewGradesApi
import net.hedtech.banner.student.history.HistoryTermForStudentGrades

import grails.converters.JSON
import groovy.json.StringEscapeUtils
import net.hedtech.banner.i18n.MessageHelper
import org.apache.commons.lang.StringUtils
import org.grails.web.json.JSONObject
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder
//import net.hedtech.banner.general.system.Term
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import net.hedtech.banner.student.history.HistoryUtility
import net.hedtech.banner.student.CourseDetailDecorator
import net.hedtech.banner.student.history.HistoryStudentCourseDetail

import net.hedtech.banner.exceptions.ApplicationException


class GradesProxyService {

    private final String GPA_TYPE_INSTITUTIONAL = 'I'
    private final String GPA_TYPE_TRANSFER = 'T'
    private final String GPA_TYPE_OVERALL = 'O'

    private final String HOURS_FORMAT = "#######0.000"

    private final String OPTION_ALL = '-1'
    private final String OPTION_COURSES_WITH_NO_STUDYPATH = '-1'
    private final String OPTION_ALL_COURSES = '-2'
    private final int OPTION_ALL_COURSES_INT = -2
    private final String INSTITUTIONAL_GPA_RULE_MAP = 'institutionalGPARuleMap'

    def proxyStudentService
    def session
    def sessionFactory


    def getViewGradesHolds(def pidm){

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def holds

        try {
            def sqlText = ViewGradesApi.VIEW_GRADES_HOLDS
            sql.call(sqlText, [pidm, Sql.VARCHAR])
                    { holdsOut ->
                        holds = holdsOut
                    }
            log.debug('finished getViewGradesHolds')
        } catch (SQLException e) {
            log.error('getViewGradesHolds() - '+ e)
            def ae = new ApplicationException( GradesProxyService.class, e )
            throw ae
        } finally {
            sql?.close()
        }

        return [viewGradesHolds: holds.equals("Y")]
    }


    def viewGrades(def params) {
        if (!proxyStudentService.checkIfStudentInstalled()) {
            return [:] as JSON
        }
        else {
            session = RequestContextHolder.currentRequestAttributes().getSession()

            def studentPidm = session."currentStudentPidm".toInteger()

            params.levelCode = OPTION_ALL
            params.studyPathCode = OPTION_ALL_COURSES

            params.pageMaxSize = 10
            params.pageOffset = 0

            def filterData = prepareFilterData(params, studentPidm)
            def pagingAndSortParams = preparePagingAndSortParams(params)
            def courses = HistoryStudentCourseDetail.fetchSearchByPidmTermLevelAndStudyPath(filterData, pagingAndSortParams)
            def courseDetails = new ArrayList(courses.size())
            courses.each { it ->
                CourseDetailDecorator courseDetail = new CourseDetailDecorator(it)
                courseDetail.campusDescription = getCampusDescription(courseDetail.campusCode)
                courseDetails.add(courseDetail)
            }
            def courseDetailsMap = getFormattedGpaInformationMap(courseDetails)
            def totalCount = HistoryStudentCourseDetail.countAllByPidmTermLevelAndStudyPath(filterData)

            def model = [
                    success    : true,
                    data       : courseDetailsMap,
                    totalCount : totalCount,
                    pageOffset : params.pageOffset ?: 0,
                    pageMaxSize: params.pageMaxSize ?: 0
            ]
            model as JSON
        }
    }


    private Map fetchFormattedGpaDetails(def gpaDetails, Map institutionGpaRule, String qualityPointsFormat, String gpaFormat) {
        DecimalFormatSymbols symbolFormatter = new DecimalFormatSymbols(LocaleContextHolder.getLocale())
        DecimalFormat hoursFormatter = new DecimalFormat(HOURS_FORMAT, symbolFormatter)
        DecimalFormat qualityPointsFormatter = new DecimalFormat(qualityPointsFormat, symbolFormatter)
        DecimalFormat gpaFormatter = new DecimalFormat(gpaFormat, symbolFormatter)
        BigDecimal qualityPoints = HistoryUtility.calculateTruncatedOrRoundedValue(institutionGpaRule?.qualityPointsDisplayNumber as Integer,
                institutionGpaRule?.qualityPointsRoundTruncateIndicator as String, gpaDetails?.qualityPoints as BigDecimal)
        BigDecimal gpa = HistoryUtility.calculateTruncatedOrRoundedValue(institutionGpaRule?.gpaDisplayNumber as Integer,
                institutionGpaRule?.gpaRoundTruncateIndicator as String, gpaDetails?.gpa  as BigDecimal)
        return [
                hoursAttempted: gpaDetails?.hoursAttempted != null ? hoursFormatter.format(gpaDetails?.hoursAttempted) : null,
                hoursEarned   : gpaDetails?.hoursEarned != null ? hoursFormatter.format(gpaDetails?.hoursEarned) : null,
                gpaHours      : gpaDetails?.gpaHours != null ? hoursFormatter.format(gpaDetails?.gpaHours) : null,
                qualityPoints : qualityPoints != null ? qualityPointsFormatter.format(qualityPoints) : null,
                gpa           : gpa != null ? gpaFormatter.format(gpa) : null
        ]
    }


    private String getGPARuleMapKey(Integer studentPidm, String termCode, String levelCode, String campusCode) {
        return "${studentPidm}_${termCode}_${levelCode}_${campusCode}".toString()
    }

    private void setGPARuleMap(Integer studentPidm, String termCode, String levelCode, String campusCode) {
        Map institutionGpaRule = HistoryUtility.fetchGpaRuleForStudent(studentPidm, levelCode, campusCode, termCode)
        String qualityPointsFormat = HistoryUtility.calculateGpaQualityPointsWebFormat(18, institutionGpaRule?.qualityPointsDisplayNumber)
        String gpaFormat = HistoryUtility.calculateGpaQualityPointsWebFormat(25, institutionGpaRule?.gpaDisplayNumber)
        String key = getGPARuleMapKey(studentPidm, termCode, levelCode, campusCode)
        Map gpaRuleMapCache = session.getAttribute(INSTITUTIONAL_GPA_RULE_MAP) ?: new HashMap<String, Object>()
        gpaRuleMapCache[key] = [institutionGpaRule: institutionGpaRule, qualityPointsFormat: qualityPointsFormat, gpaFormat: gpaFormat]
        session.setAttribute(INSTITUTIONAL_GPA_RULE_MAP, gpaRuleMapCache)
    }


    private Map getGpaRuleMap(Integer studentPidm, String termCode, String levelCode, String campusCode) {
        String key = getGPARuleMapKey(studentPidm, termCode, levelCode, campusCode)
        Map gpaRuleMap = [:]
        if (session.getAttribute(INSTITUTIONAL_GPA_RULE_MAP)) {
            gpaRuleMap = session.getAttribute(INSTITUTIONAL_GPA_RULE_MAP)[key] as Map
        }
        if (!gpaRuleMap) {
            setGPARuleMap(studentPidm, termCode, levelCode, campusCode)
            gpaRuleMap = session.getAttribute(INSTITUTIONAL_GPA_RULE_MAP)[key] as Map
        }
        return gpaRuleMap
    }


    private def getFormattedGpaInformationMap(courseDetails) {
        List courseDetailsMap = []
        courseDetails.each { courseDetail ->

            Map gpaRuleMap = getGpaRuleMap(courseDetail.studentPidm, courseDetail.termCode, courseDetail.levelCode, courseDetail.campusCode)
            Map courseDetailMap = courseDetail.properties

            Map formattedMap = fetchFormattedGpaDetails(courseDetailMap, gpaRuleMap?.institutionGpaRule, gpaRuleMap?.qualityPointsFormat, '0.00')
            courseDetailMap.putAll(formattedMap)
            courseDetailsMap.add(courseDetailMap)
        }
        return courseDetailsMap
    }


    private Map prepareFilterData(Map params, Integer studentPidm) {
        def paramsMap = [:]
        paramsMap << [studentPidm: studentPidm]
        paramsMap << [termCode: (params.termCode == OPTION_ALL) ? '%' : params.termCode]
        paramsMap << [levelCode: (params.levelCode == OPTION_ALL) ? '%' : params.levelCode]
        paramsMap << [studyPath: (!params.studyPathCode || params.studyPathCode == OPTION_ALL_COURSES) ? '%' : params.studyPathCode]
        paramsMap << [filterText: getFilterText(params.filterText)]
        return [params: paramsMap]

    }


    private Map preparePagingAndSortParams(Map params) {
        Map pagingAndSortParams = [:]
        pagingAndSortParams.sortCriteria = []
        if (params.sortColumn == null || params.sortColumn == '-1') {
            pagingAndSortParams.sortCriteria << [sortColumn: "termCode", sortDirection: "desc"]
            pagingAndSortParams.sortCriteria << [sortColumn: "levelCode", sortDirection: "asc"]
            pagingAndSortParams.sortCriteria << [sortColumn: "courseReferenceNumber", sortDirection: "asc"]
            pagingAndSortParams.sortCriteria << [sortColumn: "studyPath", sortDirection: "asc"]
        } else if (params.sortColumn == "finalGrade") {
            pagingAndSortParams.sortCriteria << [sortColumn: "calculatedFinalGrade", sortDirection: params.sortDirection]
        } else {
            pagingAndSortParams.sortCriteria << [sortColumn: params.sortColumn, sortDirection: params.sortDirection]
        }
        pagingAndSortParams << [max: params.pageMaxSize]
        pagingAndSortParams << [offset: params.pageOffset]
        return pagingAndSortParams
    }


    private static String getFilterText(filterText) {
        if (StringUtils.isBlank(filterText)) {
            filterText = '%'
        } else if (!(filterText =~ /%/)) {
            filterText = '%' + StringEscapeUtils.escapeJava(filterText) + '%'
        } else {
            filterText = StringUtils.remove(filterText, '%')
            filterText = '%' + StringEscapeUtils.escapeJava(filterText) + '%'
        }
        return filterText
    }


    private def getCampusDescription(def campCode) {
        def sql
        def stvcampRow = null
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            stvcampRow = sql.firstRow("select * from STVCAMP where STVCAMP_CODE = ?", [campCode])

        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return stvcampRow.STVCAMP_DESC
    }


}
