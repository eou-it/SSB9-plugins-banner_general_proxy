/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import net.hedtech.banner.general.person.PersonUtility

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
        def result = proxyStudentService.getCourseSchedule(37461, '09/15/2018')
        println result

        assertNotNull result.unassignedSchedule
        assertEquals 'BIOL', result.unassignedSchedule[0].crse_subj_code
        assertNotNull result.scheduleConflicts
    }


    @Test
    void testGetCourseScheduleWeekForConflicts() {
        def result = proxyStudentService.getCourseSchedule(49349, '12/26/2011')
        println result

        assertNotNull result.scheduleConflicts
        assertTrue result.scheduleConflicts.size() > 0
        assertEquals '0201', result.scheduleConflicts[0].subject
    }


    @Test
    void testGetRegistrationEventsForSchedule() {
        def schedule = [[meeting_term_code : '20140',
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
                        [meeting_term_code : '20140',
                         meeting_crn : '405',
                         meeting_begin_time: '0800',
                         meeting_end_time: '0850',
                         meeting_subj_code: 'MATH',
                         meeting_crse_numb: '405',
                         meeting_mon_day: null,
                         meeting_tue_day: null,
                         meeting_wed_day: 'W',
                         meeting_thu_day: null,
                         meeting_fri_day: null,
                         meeting_sat_day: null,
                         meeting_sun_day: null]]

        def result = proxyStudentService.getRegistrationEventsForSchedule(schedule, '09/15/2018')
        println result

        assertNotNull result.unassignedSchedule
        assertEquals 'BIOL', result.unassignedSchedule[0].crse_subj_code
    }


    @Test
    void testGetCourseSchedulDetail() {
        def result = proxyStudentService.getCourseScheduleDetail(37461)
        println result

        assertNotNull result.unassignedSchedule
        assertEquals 'BIOL', result.unassignedSchedule[0].crse_subj_code
    }

}
