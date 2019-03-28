/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.general.person.PersonUtility
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class ProxyStudentServiceIntegrationTests extends BaseIntegrationTestCase {

    def proxyStudentService
    def dataSource
    def conn

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetCourseScheduleWeek() {
        def pidm = PersonUtility.getPerson('HOS00006').pidm
        def result = proxyStudentService.getCourseSchedule(pidm, '09/15/2018')

        assertNotNull result.unassignedSchedule
        assertEquals 0, result.unassignedSchedule.size()
        assertNotNull result.schedule
        assertEquals 12, result.schedule.size()

        def course = result.schedule[0]
        assertEquals 'The Visual Arts  Studio', course.title
        assertEquals 'TBA', course.location
        assertEquals '204010', course.term
        assertEquals 'Ayeayes Selenma Term 204010', course.termDesc
        assertEquals '1', course.crn
    }

    @Test
    void testGetRegistrationEventsForSchedule() {
        def schedule = [[courseTitle : 'TEST11',
                         meeting_ssrmeet_start_date : '12/12/2012',
                         meeting_ssrmeet_end_date : '01/30/2019',
                         meeting_term_desc : 'Fake Term',
                         meeting_term_code : '20140',
                         meeting_crn : '405',
                         meeting_begin_time: '0800',
                         meeting_end_time: '0850',
                         meeting_subj_code: 'MATH',
                         meeting_crse_numb: '405',
                         meeting_mon_day: 'M',
                         meeting_tue_day: null,
                         meeting_wed_day: null,
                         meeting_thu_day: null,
                         meeting_fri_day: null,
                         meeting_sat_day: null,
                         meeting_sun_day: null],
                        [courseTitle : 'TEST01',
                         meeting_ssrmeet_start_date : '12/12/2012',
                         meeting_ssrmeet_end_date : '01/30/2019',
                         meeting_term_desc : 'Fake Term',
                         meeting_term_code : '20140',
                         meeting_crn : '405',
                         meeting_begin_time: '1400',
                         meeting_end_time: '1550',
                         meeting_subj_code: 'MATH',
                         meeting_crse_numb: '405',
                         meeting_mon_day: null,
                         meeting_tue_day: null,
                         meeting_wed_day: 'W',
                         meeting_thu_day: null,
                         meeting_fri_day: null,
                         meeting_sat_day: null,
                         meeting_sun_day: null],
                        [courseTitle : 'TEST02',
                         meeting_ssrmeet_start_date : '12/12/2012',
                         meeting_ssrmeet_end_date : '01/30/2019',
                         meeting_term_desc : 'Fake Term',
                         meeting_term_code : '20140',
                         meeting_crn : '20',
                         meeting_begin_time: '0800',
                         meeting_end_time: '0850',
                         meeting_subj_code: 'BIOL',
                         meeting_crse_numb: '405',
                         meeting_bldg_code: 'TESTB',
                         meeting_room_code: 'TESTR',
                         meeting_mon_day: 'M',
                         meeting_tue_day: null,
                         meeting_wed_day: 'W',
                         meeting_thu_day: null,
                         meeting_fri_day: null,
                         meeting_sat_day: null,
                         meeting_sun_day: null]]

        def result = proxyStudentService.getRegistrationEventsForSchedule(schedule, '09/15/2018')

        assertNotNull result.registrationEvents
        assertEquals 4, result.registrationEvents.size()
        def event = result.registrationEvents[2]

        assertEquals 'TEST02', event.title
        assertEquals '2018-09-10T08:00:00', event.start
        assertEquals '2018-09-10T08:50:00', event.end
        assertEquals '20140', event.term
        assertEquals 'BIOL', event.subject
        assertEquals '20', event.crn
        assertEquals 'TESTB TESTR', event.location
        assertTrue event.isConflicted

        event = result.registrationEvents[3]
        assertEquals 'TEST02', event.title
        assertEquals '2018-09-12T08:00:00', event.start
        assertEquals '2018-09-12T08:50:00', event.end
        assertEquals '20140', event.term
        assertEquals 'BIOL', event.subject
        assertEquals '20', event.crn
        assertEquals 'TESTB TESTR', event.location
        assertNull event.isConflicted
    }


    @Test
    void testGetCourseScheduleDetail() {
        def pidm = PersonUtility.getPerson('HOS00006').pidm
        String term = '204010'
        def result = proxyStudentService.getCourseScheduleDetail(pidm, term, null)

        def course = result.rows[0]
        assertEquals 'Ayeayes Selenma Term 204010', course.assoc_term
        assertEquals 'The Visual Arts  Studio - ART 001 - 0', course.course_title
        assertEquals '1', course.crn
        assertEquals '**Registered**', course.status_01
        assertEquals '25/07/2011', course.status_02
        assertEquals 'Rodney E. Saks', course.instructors.rows[0].instructor
        assertEquals 'Standard Letter', course.grade_mode
        assertEquals '    3.000', course.credits
        assertEquals 'Undergraduate', course.level
        assertEquals 'Main', course.campus

        def meetingTime = course.tbl_meetings[0]
        assertEquals 'Lecture', meetingTime.sched_type[0]
        assertEquals '8:00 am - 8:50 am', meetingTime.times
        assertEquals 'MWF', meetingTime.days
        assertEquals 0, meetingTime.where.size()
        assertEquals '07/25/2011', meetingTime.meet_start
        assertEquals '07/20/2012', meetingTime.meet_end
        assertEquals 'Class', meetingTime.type

    }

}
