/********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.i18n.LocalizeUtil
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.proxy.api.AccountSummaryApi
import net.hedtech.banner.proxy.api.CourseScheduleApi
import org.apache.log4j.Logger
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat

class ProxyStudentService {

    private static final log  = Logger.getLogger(GeneralSsbProxyService.class)
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def grailsApplication                  // injected by Spring

    def getAccountSummary(def pidm) {
        if (!checkIfStudentInstalled()) {
            return [:]
        }
        else {
            def accSummJson
            def sqlText = AccountSummaryApi.ACCOUNT_SUMMARY

            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [pidm, Sql.CLOB
            ]) { lv_accSumm_json ->
                accSummJson = lv_accSumm_json.asciiStream.text
            }

            def resultMap = new JsonSlurper().parseText(accSummJson)

            def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('WEBDETCODE', 'WEBACCTSUM')[0]?.external
            resultMap.showDetailCode = gtvsdaxValue == 'Y'

            resultMap
        }
    }


    def getCourseScheduleDetail(def pidm, def term, def crn) {
        if (!checkIfStudentInstalled()) {
            return [:]
        }
        else {
            def scheduleJson = ""
            def sqlText = CourseScheduleApi.WEEKLY_COURSE_SCHEDULE_DETAIL

            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [crn, term, pidm, Sql.VARCHAR
            ]) { lv_sched_json ->
                scheduleJson = lv_sched_json
            }

            def resultMap = new JsonSlurper().parseText(scheduleJson)

            return resultMap
        }
    }


    def getCourseSchedule(def pidm, def startDate = null) {
        if (!checkIfStudentInstalled()) {
            return [:]
        }
        else {
            def scheduleJson = ""
            def tbaScheduleJson = ""
            def errorMsg = ""
            def sqlText = CourseScheduleApi.WEEKLY_COURSE_SCHEDULE

            def sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.call(sqlText, [startDate, pidm, Sql.VARCHAR, Sql.VARCHAR, Sql.VARCHAR
            ]) { lv_sched_json, lv_tba_sched_json, lv_errorMsg ->
                scheduleJson = lv_sched_json
                tbaScheduleJson = lv_tba_sched_json
                errorMsg = lv_errorMsg
            }

            def scheduleJsonMap = scheduleJson ? new JsonSlurper().parseText(scheduleJson) : [:]

            if (scheduleJsonMap && !errorMsg) {
                if (!scheduleJsonMap.hasNextWeek || !scheduleJsonMap.hasPrevWeek) {
                    SimpleDateFormat usDateFmt = new SimpleDateFormat("MM/dd/yyyy")
                    Calendar startDateCal = Calendar.instance
                    Calendar schedStartDateCal = Calendar.instance
                    Calendar schedEndDateCal = Calendar.instance
                    if (startDate) {
                        startDateCal.setTime(usDateFmt.parse(startDate))
                        schedStartDateCal.setTime(usDateFmt.parse(scheduleJsonMap.schedStartDate))
                        schedEndDateCal.setTime(usDateFmt.parse(scheduleJsonMap.schedEndDate))
                        if ((!scheduleJsonMap.hasPrevWeek && startDateCal.before(schedStartDateCal)) ||
                                (!scheduleJsonMap.hasNextWeek && startDateCal.after(schedEndDateCal))) {
                            startDate = scheduleJsonMap.schedStartDate;
                        }
                    }
                }
            }

            def regEvents = getRegistrationEventsForSchedule(scheduleJsonMap.rows, startDate)

            def resultMap = [
                    schedule          : regEvents.registrationEvents,
                    dateUsed          : startDate,
                    hasNextWeek       : scheduleJsonMap.hasNextWeek,
                    hasPrevWeek       : scheduleJsonMap.hasPrevWeek,
                    unassignedSchedule: new JsonSlurper().parseText(tbaScheduleJson).rows,
                    errorMsg          : errorMsg
            ]

            return resultMap
        }
    }

    private getRegistrationEventsForSchedule(def weeklySchedule, def startDate) {
        def registrationArray = []
        def conflictingEvents = []
        def eventsAlreadyOnSchedule = [
                (Calendar.MONDAY):    [],
                (Calendar.TUESDAY):   [],
                (Calendar.WEDNESDAY): [],
                (Calendar.THURSDAY):  [],
                (Calendar.FRIDAY):    [],
                (Calendar.SATURDAY):  [],
                (Calendar.SUNDAY):    []
        ]

        SimpleDateFormat usDateFmt = new SimpleDateFormat("MM/dd/yyyy")

        Calendar startDateCal = Calendar.instance
        if(startDate) {
            startDateCal.setTime(usDateFmt.parse(startDate))
        }
        def id = new Date().getTime()
        weeklySchedule.each {

            //SSRMEET dates should never be null but use a default date far in the past or future just in case
            Calendar ssrmeetStartCal = Calendar.instance
            ssrmeetStartCal.setTime(usDateFmt.parse(it.meeting_ssrmeet_start_date ?: '12/31/5000'))

            Calendar ssrmeetEndCal = Calendar.instance
            ssrmeetEndCal.setTime(usDateFmt.parse(it.meeting_ssrmeet_end_date ?: '01/02/1970'))

            if(it.meeting_begin_time && it.meeting_end_time) {
                if (it.meeting_mon_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.MONDAY,    eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_tue_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.TUESDAY,   eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_wed_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.WEDNESDAY, eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_thu_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.THURSDAY,  eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_fri_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.FRIDAY,    eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_sat_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.SATURDAY,  eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
                if (it.meeting_sun_day) {
                    addRegistrationEvent(it, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, Calendar.SUNDAY,    eventsAlreadyOnSchedule, registrationArray, conflictingEvents)
                }
            }
        }

        registrationArray.each {
            it.remove('startCal')
            it.remove('endCal')
        }
        return [registrationEvents: registrationArray]
    }

    private addRegistrationEvent(event, id, startDateCal, ssrmeetStartCal, ssrmeetEndCal, dayOfWeek, eventsAlreadyOnSchedule, registrationArray, conflictingEvents) {
        startDateCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        boolean isDateWithinMeetingDates = !startDateCal.before(ssrmeetStartCal) && !startDateCal.after(ssrmeetEndCal)

        if(isDateWithinMeetingDates) {
            def regEvent = createRegistrationEvent(id, event.meeting_term_code, event.meeting_term_desc, event.meeting_crn, event.courseTitle,
                    startDateCal, event.meeting_begin_time, event.meeting_end_time, 'proxy-event', event.meeting_subj_code,
                    event.meeting_crse_numb)

            if(event.meeting_bldg_code) {
                regEvent.location = event.meeting_bldg_code + ' ' + event.meeting_room_code
            }
            else {
                regEvent.location = MessageHelper.getMessage('proxy.schedule.Tba')
            }

            if (registrationEventTimesConflict(regEvent, eventsAlreadyOnSchedule[dayOfWeek])) {
                regEvent.isConflicted = true
            }

            registrationArray.add(regEvent)
            eventsAlreadyOnSchedule[dayOfWeek].add(regEvent)
        }
    }

    private def createRegistrationEvent(id, term, termDesc, crn, title, date, beginTime, endTime, className, subject = null, courseNumber = null) {
        Calendar startCal = Calendar.instance
        Calendar endCal = Calendar.instance
        startCal.setTime(date.getTime())
        endCal.setTime(date.getTime())
        startCal.set(Calendar.HOUR_OF_DAY, beginTime.substring(0, 2).toInteger())
        startCal.set(Calendar.MINUTE, beginTime.substring(2, 4).toInteger())
        def registrationMap = [:]
        registrationMap.id = id
        registrationMap.title = title
        registrationMap.startCal = startCal
        registrationMap.start = startCal.time.format("yyyy-MM-dd'T'HH:mm:ss")
        endCal.set(Calendar.HOUR_OF_DAY, endTime.substring(0, 2).toInteger())
        endCal.set(Calendar.MINUTE, endTime.substring(2, 4).toInteger())
        registrationMap.endCal = endCal
        registrationMap.end = endCal.time.format(LocalizeUtil.message("default.date.time.ISO8601.format", null, LocaleContextHolder.getLocale()))
        registrationMap.editable = false
        registrationMap.allDay = false
        registrationMap.className = className
        registrationMap.term = term
        registrationMap.termDesc = termDesc
        registrationMap.crn = crn
        registrationMap.subject = subject ?: ""
        registrationMap.courseNumber = courseNumber ?: ""
        return registrationMap
    }

    private registrationEventTimesConflict(event, existingEvents) {
        def hasConflict = false

        existingEvents.reverse().find {
            if ( !(event.startCal.before(it.startCal) || event.startCal.after(it.endCal)) ) {
                hasConflict = true
                return true
            }
        }

        hasConflict
    }

    def checkIfStudentInstalled() {

        boolean isStudentInstalled
        def session = RequestContextHolder?.currentRequestAttributes()?.request?.session

        if (session?.getAttribute("isStudentInstalled") != null) {
            isStudentInstalled = session.getAttribute("isStudentInstalled")
        }else {
            isStudentInstalled = InstitutionalDescription.fetchByKey()?.studentInstalled
            session.setAttribute("isStudentInstalled",isStudentInstalled)
        }
        return isStudentInstalled
    }

}
