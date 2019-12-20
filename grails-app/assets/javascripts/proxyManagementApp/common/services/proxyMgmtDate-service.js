/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyManagementApp.service('proxyMgmtDateService', ['$filter',
    function ($filter) {
        var dateFmt,
            calendar = (function () {
                var locale = $('meta[name=locale]').attr("content");

                if (locale.split('-')[0] === 'ar') {
                    dateFmt = $filter('i18n')('default.date.format');
                    return $.calendars.instance('islamic');
                } else {
                    dateFmt = $filter('i18n')('default.date.format').toLowerCase();
                    dateFmt = dateFmt.replace(/mmm/i, 'M'); // short month format is M, not MMM, for jQuery calendar
                    return $.calendars.instance();
                }
            }());
        this.dateFieldIsEmpty = function (date) {
            return !date
        };
        this.stopDateIsBeforeStartDate = function (startDate, stopDate) {
            var MAX_DATE = 8640000000000000,
                fromDate = this.stringToDate(startDate),
                toDate = stopDate ? this.stringToDate(stopDate) : new Date(MAX_DATE);
            return fromDate > toDate;
        };
        this.dateFormatIsInvalid = function (date) {
            return (date && !this.stringToDate(date));
        };
        this.stringToDate = function (date) {
            var result;
            try {
                result = calendar.parseDate(dateFmt, date).toJSDate();
                return result;
            } catch (exception) {
                return null;
            }
        };
    }
]);
