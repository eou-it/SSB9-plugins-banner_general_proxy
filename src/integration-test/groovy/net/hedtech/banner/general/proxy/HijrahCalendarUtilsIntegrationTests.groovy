/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Test

@Integration
@Rollback
class HijrahCalendarUtilsIntegrationTests extends BaseIntegrationTestCase{

    def implementedPattern = 'MM/dd/yyyy HH:mm'
    def patternNotImplemented = 'dd/MM/yyyy'

    def datesThatShouldMatchPattern = [
            '01/01/0001 00:01',
            '12/31/2019 23:59',
            '01/01/1999 12:00'
    ]

    def datesThatShouldNotMatchPattern = [
            '00/00/0000 00:00',
            '1',
            '',
            '13/30/1999 12:21',
            '12/33/1999 12:21',
            '12/12/1999 25:00'
    ]

    @Test
    void testDateStringMatchesRequiredPattern () {

        String nullString = null

        for (date in datesThatShouldMatchPattern) {
            assertTrue(HijrahCalendarUtils.dateStringMatchesRequiredPattern(date, implementedPattern) == true)
        }

        for (date in datesThatShouldNotMatchPattern) {
            assertFalse(HijrahCalendarUtils.dateStringMatchesRequiredPattern(date, implementedPattern) == true)
        }

        assertFalse(HijrahCalendarUtils.dateStringMatchesRequiredPattern(nullString, implementedPattern) == true)

        assertFalse(HijrahCalendarUtils.dateStringMatchesRequiredPattern(datesThatShouldMatchPattern[0], patternNotImplemented) == true)

    }

    @Test
    void testGetHijrahDateWithTimestampFromString() {
        def gregorianDates = [
                datesThatShouldMatchPattern[1],
                datesThatShouldMatchPattern[2]
                ]
        def expectedHijrahDates = [
                "05/جمادى الأولى/1441 23:59",
                "13/رمضان/1419 12:00"
                ]

        for (def i = 0; i < gregorianDates.size(); i++) {
            assertEquals(HijrahCalendarUtils.getHijrahDateWithTimestampFromString(gregorianDates[i]), expectedHijrahDates[i])
        }
    }

}
