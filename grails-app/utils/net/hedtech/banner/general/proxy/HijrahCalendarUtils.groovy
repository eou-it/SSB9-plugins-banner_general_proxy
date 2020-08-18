/********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.i18n.DateConverterService
import net.hedtech.banner.i18n.MessageHelper
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Slf4j
class HijrahCalendarUtils {
    static def dateConverterService = new DateConverterService()

    private static final def DATE_STRING_WITH_TIMESTAMP = 'MM/dd/yyyy HH:mm'
    private static final def TIMESTAMP_FORMAT = 'HH:mm'

    /**
     * This method will accept a date that is either an English representation of a Hijrah date, or a Gregorian date
     * and will convert the date to the Hijrah calendar, if not already in the Hijrah calendar, translate the date to Arabic,
     * and format the date based on the default.dateshorttime.format or default.date.format for the user's locale.
     *
     * @param date - The String date which is getting converted / formatted to the Hijrah date and format.
     * @param fromPattern - The String pattern which is the pattern of the date passed to the method.
     * @param addTimestampWithToDate - A boolean which determines if the returned Hijrah date should have a timestamp.
     * @returns A String date which has been converted and formatted to a Hijrah date.*/
    static def getFormattedArabicDate(date, fromPattern, addTimestampWithToDate) {
        final def GREGORIAN_U_LOCALE_STRING = "en_US@calendar=gregorian"
        final def ISLAMIC_U_LOCALE_STRING = "en_US@calendar=islamic"
        def localeString = LocaleContextHolder.getLocale().toString()
        def fromULocaleString
        DateTimeFormatter hijrahFormatter = DateTimeFormatter.ofPattern(fromPattern)
                .withChronology(HijrahChronology.INSTANCE);
        try {
            HijrahDate hijrahDate = hijrahFormatter.parse(date, HijrahDate.&from);
            fromULocaleString = ISLAMIC_U_LOCALE_STRING
        } catch (DateTimeParseException e) {
            log.info(date + ", which was being parsed in Proxy Management, is not a valid Hijrah date.")
            fromULocaleString = GREGORIAN_U_LOCALE_STRING
        }
        finally {
            if (fromULocaleString) {
                return date ?
                        dateConverterService.convert(date,
                                fromULocaleString,
                                localeString + "@calendar=islamic",
                                fromPattern,
                                getDateFormat(addTimestampWithToDate))
                        : date
            } else {
                return date
            }
        }
    }

    def static getDateFormat(boolean withTimestamp) {
        def key = withTimestamp ? "default.dateshorttime.format" : "default.date.format"
        message(key, null, LocaleContextHolder.getLocale())
    }

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
            if (requiredPattern == DATE_STRING_WITH_TIMESTAMP) {
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

    private static String message(key, args = null, locale = null) {
        // copied from banner-general:net.hedtech.banner.MessageUtility rather than introducing new plugin-plugin dependency

        String value = "";
        if (key) {
            if (!locale) locale = Locale.getDefault()
            MessageSource messageSource = Holders.grailsApplication.mainContext.getBean("messageSource")
            value = messageSource.getMessage(key, args, locale)
        }
        return value
    }
}
