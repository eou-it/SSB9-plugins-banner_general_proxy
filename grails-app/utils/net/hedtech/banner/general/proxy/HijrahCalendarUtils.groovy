/********************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter

class HijrahCalendarUtils {

    private static final def DATE_STRING_WITH_TIMESTAMP = 'MM/dd/yyyy HH:mm'
    private static final def TIMESTAMP_FORMAT = 'HH:mm'

    //String passed into this method should be formatted 'MM/dd/yyyy HH:mm'
    static def getHijrahDateWithTimestampFromString (String dateAndTimeString) {

        if (dateStringMatchesRequiredPattern(dateAndTimeString, DATE_STRING_WITH_TIMESTAMP)) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_STRING_WITH_TIMESTAMP)
            Date gregorianDate = dateFormat.parse(dateAndTimeString)
            DateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, new Locale("en"))
            String formattedTimestamp = timestampFormat.format(gregorianDate)

            Calendar gregorianCalendar = getGregorianCalendarFromDate(gregorianDate)
            HijrahDate hijrahDate = getHijrahDateFromGregorianCalendar(gregorianCalendar)

            return DateTimeFormatter.ofPattern("dd/MMM/yyyy", new Locale(
                    "ar")).format(hijrahDate)+ " " + formattedTimestamp

        }
        else {
            throw new DateTimeException("The date provided to HijrahCalendarUtils does not match the pattern required" +
            " to be converted to a Hirjah date.")
        }
    }

    static def dateStringMatchesRequiredPattern (String dateString, String requiredPattern) {
        if (dateString == null || requiredPattern == null) {
            return false
        }
        else {
            if (requiredPattern == 'MM/dd/yyyy HH:mm') {
                return dateString.matches('^([0]?[1-9]|1[012])/([1-9]|([12][0-9])|[0][1-9]|(3[01]))/\\d\\d\\d\\d (20|21|22|23|[0-1]?\\d):[0-5]?\\d')
            }
        }
    }

    private static def getGregorianCalendarFromDate (Date date) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        return calendar
    }

    private static def getHijrahDateFromGregorianCalendar (Calendar gregorianCalendar) {
        return HijrahChronology.INSTANCE.date(LocalDateTime.of(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH)+1,
                gregorianCalendar.get(Calendar.DATE),
                gregorianCalendar.get(Calendar.HOUR_OF_DAY),
                gregorianCalendar.get(Calendar.MINUTE)))
    }
}
