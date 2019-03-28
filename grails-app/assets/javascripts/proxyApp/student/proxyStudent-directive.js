/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('fullCalendar',['proxyAppService', '$filter', '$compile', '$rootScope', function(proxyAppService, $filter, $compile, $rootScope) {

    var goToText = $filter('i18n')('proxy.schedule.goTo'),
        datePlaceholderText = $filter('i18n')('default.date.format.watermark'),
        titleFormat,
        mobileOptions = {
            defaultView: 'agendaTwoDay',
            aspectRatio: 0.8,
            header: {
                left: 'title',
                center: '',
                right: ''
            },
            footer: {
                right: '',
                center: 'prevWeek prev next nextWeek'
            }
        },
        desktopOptions = {
            defaultView: 'agendaSevenDay',
            aspectRatio: 2,
            header: {
                left: '',
                center: 'title',
                right: ''
            },
            footer: {
                center: '',
                right: 'prev next'
            }
        },
        locale = $('meta[name=locale]').attr("content"),
        lang = locale.split('-')[0],
        meridiem = [$filter('i18n')('default.time.am'), $filter('i18n')('default.time.pm')],

        hasEventWithinPast2Days = function () {
            var fc = $('#calendar'),
                calDate = fc.fullCalendar('getDate').hour(0);

            return fc.fullCalendar('clientEvents', function(event) {
                return (event.start.isBefore(calDate));
            }).length > 0;
        },

        hasEventWithinNext2Days = function () {
            var fc = $('#calendar'),
                targetCalDate = fc.fullCalendar('getDate').add(2, 'days').hour(0);

            return fc.fullCalendar('clientEvents', function(event) {
                return (event.end.isSameOrAfter(targetCalDate));
            }).length > 0;
        },

        doConvert = function (config, date) {
            var convertedDate = '';

            config.date = date;

            $.ajax({
                url: 'dateConverter',
                type: 'GET',
                async: false,
                timeout: 1000,
                data: config,
                success: function(result) {
                    convertedDate = result;
                },
                error: function (jqXHR, textStatus) {
                    throw new Error('Failed to convert date: ' + textStatus);
                }
            });

            delete config.date;

            return convertedDate;
        },

        // Load localization meridiem (AM/PM) overrides as translated in Platform properties.
        // Regarding the code in this function:  This code was pulled from one of the FullCalendar locale files, i.e.
        // fullcalendar-3.9.0/locale/ar.js, which I was unfortunately only able to find in minified form.  Anything
        // found to be unneeded for the immediate purpose at hand was stripped out, and other relevant modifications
        // were made.  See https://fullcalendar.io/docs/locale for more information.
        loadLocalization = function (locale, meridiem) {
            !function (e, t) {
                "object" == typeof exports && "object" == typeof module ?
                                module.exports = t(require("moment"), require("fullcalendar")) : "function" == typeof define && define.amd ?
                                    define(["moment", "fullcalendar"], t) : "object" == typeof exports ?
                                        t(require("moment"), require("fullcalendar")) : t(e.moment, e.FullCalendar)
            }("undefined" != typeof self ? self : this, function (e, t) {
                return function (e) {
                    function t(n) {
                        if (r[n]) return r[n].exports;
                        var o = r[n] = {
                            i: n,
                            l: !1,
                            exports: {}
                        };
                        return e[n].call(o.exports, o, o.exports, t), o.l = !0, o.exports
                    }

                    var r = {};
                    return t.m = e, t.c = r, t.d = function (e, r, n) {
                        t.o(e, r) || Object.defineProperty(e, r, {
                            configurable: !1,
                            enumerable: !0,
                            get: n
                        })
                    }, t.n = function (e) {
                        var r = e && e.__esModule ? function () {
                            return e.default
                        } : function () {
                            return e
                        };
                        return t.d(r, "a", r), r
                    }, t.o = function (e, t) {
                        return Object.prototype.hasOwnProperty.call(e, t)
                    }, t.p = "", t(t.s = 79)
                }({
                    0: function (t, r) {
                        t.exports = e
                    },
                    1: function (e, r) {
                        e.exports = t
                    },
                    79: function (e, t, r) {
                        Object.defineProperty(t, "__esModule", {
                            value: !0
                        }), r(80);
                        var n = r(1);
                        n.locale(locale, {})
                    },
                    80: function (e, t, r) {
                        !function (e, t) {
                            t(r(0))
                        }(0, function (e) {
                            return e.updateLocale(locale, {
                                meridiem: function (e, t, r) {
                                    return e < 12 ? meridiem[0] : meridiem[1]
                                }
                            })
                        })
                    }
                })
            });
        };

    // Load the current locale override, which FullCalendar will use by default, i.e. it doesn't need to be specified
    // to FullCalendar with a "locale" option (per https://fullcalendar.io/docs/locale: "If you are simply loading one
    // locale, you do not need to specify the locale option. FullCalendar will look at the most recent locale file
    // loaded and use it.").
    loadLocalization(locale, meridiem);

    // Convert date format to MomentJS date format (used by FullCalendar)
    if (lang === 'ar') {
        // Message property specified as correct for Arabic
        titleFormat = $filter('i18n')('default.date.format');

        // This formatRange override converts the dates in the calendar TITLE to Hijri.
        $.fullCalendar.View.mixin({
            formatRange: function (range, isAllDay, formatStr, separator) {
                var end = range.end,
                    startConverted,
                    endConverted,
                    fromFormat = 'MM/DD/YYYY',
                    convertConfig = {
                        fromDateFormat: 'MM/dd/yyyy',
                        fromULocale:    'ar_US@calendar=gregorian',
                        toULocale:      'ar@calendar=islamic-civil',
                        toDateFormat:   titleFormat
                    };

                if (isAllDay) {
                    end = end.clone().subtract(1); // convert to inclusive. last ms of previous day
                }

                startConverted = doConvert(convertConfig, range.start.format(fromFormat));
                endConverted   = doConvert(convertConfig, end.format(fromFormat));

                return startConverted + ' - ' + endConverted;
            }
        });

        // This renderHeadDateCellHtml override converts the dates in the calendar COLUMN HEADINGS to Hijri.
        $.fullCalendar.TimeGrid.mixin({
            renderHeadDateCellHtml: function (date, colspan, otherAttrs) {
                var convertedDate,
                    fromFormat = 'MM/DD/YYYY',
                    convertConfig = {
                        fromDateFormat: 'MM/dd/yyyy',
                        fromULocale: 'ar_US@calendar=gregorian',
                        toULocale: 'ar@calendar=islamic-civil',
                        toDateFormat: 'dd EEEE'
                    },
                    htmlEscape = function(s) {
                        return (s + '').replace(/&/g, '&amp;')
                            .replace(/</g, '&lt;')
                            .replace(/>/g, '&gt;')
                            .replace(/'/g, '&#039;')
                            .replace(/"/g, '&quot;')
                            .replace(/\n/g, '<br />');
                    };

                var t = this;
                var view = t.view;
                var isDateValid = t.dateProfile.activeUnzonedRange.containsDate(date); // TODO: called too frequently. cache somehow.
                var classNames = [
                    'fc-day-header',
                    view.calendar.theme.getClass('widgetHeader')
                ];
                var innerHtml;
                if (typeof t.opt('columnHeaderHtml') === 'function') {
                    innerHtml = t.opt('columnHeaderHtml')(date);
                }
                else if (typeof t.opt('columnHeaderText') === 'function') {
                    innerHtml = htmlEscape(t.opt('columnHeaderText')(date));
                }
                else {
                    // Convert column head to valid Hijri day-of-week and day-in-month
                    convertedDate = doConvert(convertConfig, date.format(fromFormat));
                    innerHtml = htmlEscape(convertedDate);
                }
                // if only one row of days, the classNames on the header can represent the specific days beneath
                if (t.rowCnt === 1) {
                    classNames = classNames.concat(
                        // includes the day-of-week class
                        // noThemeHighlight=true (don't highlight the header)
                        t.getDayClasses(date, true));
                }
                else {
                    classNames.push('fc-' + util_1.dayIDs[date.day()]); // only add the day-of-week class
                }
                return '' +
                    '<th class="' + classNames.join(' ') + '"' +
                    ((isDateValid && t.rowCnt) === 1 ?
                        ' data-date="' + date.format('YYYY-MM-DD') + '"' :
                        '') +
                    (colspan > 1 ?
                        ' colspan="' + colspan + '"' :
                        '') +
                    (otherAttrs ?
                        ' ' + otherAttrs :
                        '') +
                    '>' +
                    (isDateValid ?
                        // don't make a link if the heading could represent multiple days, or if there's only one day (forceOff)
                        view.buildGotoAnchorHtml({ date: date, forceOff: t.rowCnt > 1 || t.colCnt === 1 }, innerHtml) :
                        // if not valid, display text, but no link
                        innerHtml) +
                    '</th>';
            }
        });
    } else if (lang === 'pt') {
        // Message property specified as correct for Portuguese
        titleFormat = $filter('i18n')('communication.default.dateFormat');

        titleFormat = titleFormat.replace(/''/g, "'")  //use "'" for literal ' instead "''"
            .replace(/'([^']+)'/g, '[$1]')             //use "[...]" for literals instead "'...'"
            .replace(/d(?![^\[]*\])/g, 'D')            //use "D" for short day of month instead "d"
            .replace(/yyyy/g, 'YYYY');                 //use "YYYY" for 4-digit year instead "yyyy"
    } else {
        // Message property specified as correct for all other locales
        titleFormat = $filter('i18n')('js.datepicker.tooltipDateFormat');

        titleFormat = titleFormat.replace(/''/g, "'")  //use "'" for literal ' instead "''"
            .replace(/'([^']+)'/g, '[$1]')             //use "[...]" for literals instead "'...'"
            .replace(/d(?![^\[]*\])/g, 'D')            //use "D" for short day of month instead "d"
            .replace(/DD/g, 'dddd')                    //use "dddd" for long day of week instead "DD"
            .replace(/yyyy/g, 'YYYY');                 //use "YYYY" for 4-digit year instead "yyyy"
    }

    return {
        link: function(scope, elem, attrs) {
            var isMobile = $rootScope.isMobileView(),
                isRTL = $.i18n.prop('default.language.direction') === 'rtl',
                firstDay = $.i18n.prop('default.firstDayOfTheWeek'),
                columnHeaderFormat = (lang === 'es') ? 'ddd DD' : 'DD ddd', // Specified by translation team

                // Platform provides the default.time.format property, which should be adjusted for a 12/24 hour clock.
                // However, at least for en-US (default messages.properties file), that property doesn't include AM/PM
                // in the format, so here we pick the best format based on the 12/24 hour clock.
                clockFormat = $filter('i18n')('default.timebox.clock.format'), // 12 or 24
                timeFormatKey = (clockFormat && clockFormat === '24') ? 'timebox.24hr.format' : 'default.time.12hour.display.format',
                timeFormat = $filter('i18n')(timeFormatKey),

                addDatePicker = function() {
                    var datePickerTemplate = '<div class="gotodate-block"> <label>' + goToText + '</label> <input date-picker ng-model="tgtDate" pi-input-watcher on-select="goToDate" class="eds-text-field pi-date-input input-colors" placeholder="' + datePlaceholderText + '" id="goToDate"/> </div>',
                        datePickerElem = $compile(datePickerTemplate)(scope);
                    $('.fc-header-toolbar > .fc-right').append(datePickerElem);
                };

                $('#calendar').fullCalendar({
                    customButtons: {
                        nextWeek: {
                            text: isRTL ? '<<' : '>>',
                            click: function() {
                                var tgtDate = $('#calendar').fullCalendar('getDate').add(7, 'days');
                                $('#calendar').fullCalendar('gotoDate', tgtDate);
                            }
                        },
                        prevWeek: {
                            text: isRTL ? '>>' : '<<',
                            click: function() {
                                var tgtDate = $('#calendar').fullCalendar('getDate').subtract(7, 'days');
                                $('#calendar').fullCalendar('gotoDate', tgtDate);
                            }
                        }
                    },
                    header: isMobile ? mobileOptions.header : desktopOptions.header,
                    footer: isMobile ? mobileOptions.footer : desktopOptions.footer,
                    defaultView: isMobile ? mobileOptions.defaultView : desktopOptions.defaultView,
                    views: {
                        agendaSevenDay: {
                            type: 'agenda',
                            duration: { weeks: 1 }
                        },
                        agendaTwoDay: {
                            type: 'agenda',
                            duration: { days: 2 }
                        }
                    },
                    aspectRatio: isMobile ? mobileOptions.aspectRatio : desktopOptions.aspectRatio,
                    allDaySlot: false,
                    editable: false,
                    eventColor: '#eff7ff',
                    eventTextColor: '#2874BB',
                    eventBorderColor: '#2874bb',
                    slotEventOverlap: false,
                    slotDuration: '00:15:00',
                    slotLabelInterval: '01:00',
                    slotLabelFormat: timeFormat,
                    scrollTime: '08:00:00',
                    columnHeaderFormat: columnHeaderFormat,
                    timeFormat: timeFormat,
                    titleFormat: titleFormat,
                    firstDay: (!firstDay || isNaN(firstDay)) ? 0 : firstDay,
                    isRTL: isRTL,
                    dayNames: $.i18n.prop("default.gregorian.dayNames").split(','),
                    dayNamesShort: $.i18n.prop("default.gregorian.dayNamesShort").split(','),
                    monthNames: $.i18n.prop("default.gregorian.monthNames").split(','),
                    monthNamesShort: $.i18n.prop("default.gregorian.monthNamesShort").split(','),
                    events: function (start, end, timezone, callback) {
                        var events;
                        proxyAppService.getCourseSchedule({id: scope.id, date: start.format('MM/DD/YYYY')}).$promise.then(function(response) {
                            events = response.schedule;
                            scope.hasNextWeek = response.hasNextWeek;
                            scope.hasPrevWeek = response.hasPrevWeek;
                            scope.unassignedSchedule = response.unassignedSchedule;
                            scope.hasDetailAccess = response.hasDetailAccess;
                            scope.errorMsg = response.errorMsg;
                            callback(events);

                            var dateUsed = moment(response.dateUsed, 'MM/DD/YYYY');
                            if(dateUsed != start) {
                                $('#calendar').fullCalendar( 'gotoDate', dateUsed);
                            }
                        });
                    },
                    eventRender: function (event, element, view) {
                        var options = {},
                            html = event.isConflicted ? '<span class="icon-info-CO"></span>' : '',
                            courseLinkTemplate = scope.hasDetailAccess ?
                                html+'<a ui-sref="/ssb/proxy/courseScheduleDetail({crn: '+ event.crn +', termCode: '+ event.term +', termDesc: \''+ event.termDesc +'\'})"> '+ event.title +'</a><br><span>'+ event.location +'</span>' :
                                html+'<span> '+ event.title +'</span><br><span>'+ event.location +'</span>',
                            courseLinkElem = $compile(courseLinkTemplate)(scope);

                        $('.fc-title', element).text("");
                        $('.fc-title', element).append(courseLinkElem);
                    },
                    eventAfterAllRender: function (view) {
                        var prevBtnElem = $('.fc-prev-button'),
                            nextBtnElem = $('.fc-next-button'),
                            backwardElem = null,
                            forwardElem = null;

                        if(view.name === 'agendaTwoDay')  {
                            backwardElem = $('.fc-prevWeek-button');
                            forwardElem = $('.fc-nextWeek-button');
                            if(scope.hasPrevWeek) {
                                backwardElem.removeClass('fc-state-disabled');
                                backwardElem.removeAttr('disabled');
                            }
                            else {
                                backwardElem.addClass('fc-state-disabled');
                                backwardElem.attr('disabled', '');

                                if(hasEventWithinPast2Days()) {
                                    prevBtnElem.removeClass('fc-state-disabled');
                                    prevBtnElem.removeAttr('disabled');
                                }
                                else {
                                    prevBtnElem.addClass('fc-state-disabled');
                                    prevBtnElem.attr('disabled', '');
                                }
                            }
                            backwardElem.addClass('secondary');

                            if(scope.hasNextWeek) {
                                forwardElem.removeClass('fc-state-disabled');
                                forwardElem.removeAttr('disabled');
                            }
                            else {
                                forwardElem.addClass('fc-state-disabled');
                                forwardElem.attr('disabled', '');

                                if(hasEventWithinNext2Days()) {
                                    nextBtnElem.removeClass('fc-state-disabled');
                                    nextBtnElem.removeAttr('disabled');
                                }
                                else {
                                    nextBtnElem.addClass('fc-state-disabled');
                                    nextBtnElem.attr('disabled', '');
                                }
                            }
                            forwardElem.addClass('secondary');
                        }
                        else {

                            if (scope.hasPrevWeek) {
                                prevBtnElem.removeClass('fc-state-disabled');
                                prevBtnElem.removeAttr('disabled');
                            }
                            else {
                                prevBtnElem.addClass('fc-state-disabled');
                                prevBtnElem.attr('disabled', '');
                            }

                            if (scope.hasNextWeek) {
                                nextBtnElem.removeClass('fc-state-disabled');
                                nextBtnElem.removeAttr('disabled');
                            }
                            else {
                                nextBtnElem.addClass('fc-state-disabled');
                                nextBtnElem.attr('disabled', '');
                            }
                        }
                        prevBtnElem.addClass('secondary');
                        nextBtnElem.addClass('secondary');
                    },
                    windowResize: function(view) {
                        var fc = $('#calendar');
                        if ($rootScope.isMobileView()) {
                            if(view.name === 'agendaSevenDay') {
                                fc.fullCalendar('changeView', 'agendaTwoDay');
                                fc.fullCalendar('option', 'aspectRatio', mobileOptions.aspectRatio);
                                fc.fullCalendar('option', 'header', mobileOptions.header);
                                fc.fullCalendar('option', 'footer', mobileOptions.footer);

                                addDatePicker();
                            }
                        }
                        else {
                            if(view.name === 'agendaTwoDay') {
                                fc.fullCalendar('changeView', 'agendaSevenDay');
                                fc.fullCalendar('option', 'aspectRatio', desktopOptions.aspectRatio);
                                fc.fullCalendar('option', 'header', desktopOptions.header);
                                fc.fullCalendar('option', 'footer', desktopOptions.footer);

                                addDatePicker();
                            }
                        }
                    }
                });

            addDatePicker();
        }
    };
}]);

proxyAppDirectives.directive('setupTermSelector', ['proxyAppService', function(proxyAppService) {
    return {
        restrict: 'A',
        link: function(scope, elem) {
            elem.on('change', function (event) {
                proxyAppService.getGrades({termCode: event.target.value}).$promise.then(function(response) {
                    scope.student.grades = response.data;

                    proxyAppService.setTerm(scope.term.code);
                });
            });
        }
    };
}]);
