/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('fullCalendar',['proxyAppService', function(proxyAppService) {
    var scrollToFirstEvent = _.debounce(function() {
        var top = topOf('.fc-event:visible');
        if (top != null) {
            var scrollable = $('.fc-event').closest('[style*=overflow-y]');
            scrollable.stop().animate({scrollTop:top});
        }
    }, 100);

    return {
        link: function(scope, elem, attrs) {
            //function setupCalendarView() {
            //    var amPm = $.i18n.prop("default.gregorian.amPm").toLowerCase().split(',');
            //    $.fullCalendar.dateFormatters['tt'] = function (d) {
            //        return d.getHours() < 12 ? amPm[0] : amPm[1];
            //    };

                $('#calendar').fullCalendar({
                    header: {
                        left: '',
                        center: '',
                        right: ''
                    },
                    defaultView: 'agendaWeek',
                    allDaySlot: false,
                    editable: false,
                    //columnFormat: {
                    //    agendaWeek: 'dddd',
                    //    basicWeek: 'ddd'
                    //},
                    columnHeaderFormat: 'dddd',
                    //timeFormat: {
                    //    basicWeek: 'h:mm{-h:mm}' // 5:00 - 6:30
                    //},
                    timeFormat: 'h:mm', // 5:00 - 6:30,
                    titleFormat: '[Week of] MMMM DD, YYYY',
                    header: {
                        left: 'prev',
                        center: 'title',
                        right: 'next'
                    },

                    firstDay: parseInt($.i18n.prop("default.firstDayOfTheWeek")),
                    dayNames: ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],//$.i18n.prop("default.gregorian.dayNames").split(','), TODO: add to props
                    dayNamesShort: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],//$.i18n.prop("default.gregorian.dayNamesShort").split(','), TODO: add to props
                    monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'], //$.i18n.prop("default.gregorian.monthNames").split(','), TODO: add to props
                    monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'], //$.i18n.prop("default.gregorian.monthNamesShort").split(','),
                    axisFormat: $.i18n.prop('events.row.time.format'),
                    isRTL: $.i18n.prop('default.language.direction') == 'rtl',
                    events: function (start, end, timezone, callback) {
                        //var events = JSON.parse(sessionStorage.getItem("classScheduleEvents"));
                        start.add(1, 'days'); //move start to a Monday to coincide with SQL date processing
                        var events;
                        proxyAppService.getCourseSchedule({pidm: attrs.pidm, date: start.format('MM/DD/YYYY')}).$promise.then(function(response) {
                            events = response.schedule;
                            scope.hasNextWeek = response.hasNextWeek;
                            scope.hasPrevWeek = response.hasPrevWeek;
                            callback(events);

                            var dateUsed = moment(response.dateUsed, 'MM/DD/YYYY');
                            if(dateUsed != start) {
                                $('#calendar').fullCalendar( 'gotoDate', dateUsed);
                            }
                        });
                    },
                    eventRender: function (event, element, view) {
                        var options = {};
                        options.term = event.term;
                        options.courseReferenceNumber = event.crn;
                        options.courseTitle = event.subject + " " + event.courseNumber;
                        $('.fc-title', element).text("");
                        //$('.fc-event-title', element).html(setupCourseDetailsLink(options));
                        $('.fc-title', element).html("<a>"+ options.courseTitle +"</a>");

                        /*if (view.name == "agendaWeek") {
                            var options = {};
                            options.term = event.term;
                            options.courseReferenceNumber = event.crn;
                            options.courseTitle = event.subject + " " + event.courseNumber;
                            $('.fc-event-title', element).text("");
                            //$('.fc-event-title', element).html(setupCourseDetailsLink(options));
                            $('.fc-event-title', element).html("<a>"+ options.courseTitle +"</a>");
                        }
                        else {
                            var options = {};
                            options.term = event.term;
                            options.courseReferenceNumber = event.crn;
                            options.courseTitle = event.subject + " " + event.courseNumber;
                            $(".fc-event-time").show();
                            $('.fc-event-title', element).text("");
                            //$('.fc-event-title', element).html("<br>" + setupCourseDetailsLink(options));
                            $('.fc-event-title', element).html("<a>"+ options.courseTitle +"</a>");
                        }*/
                    },
                    eventAfterRender: function (event, element, view) {
                        // this is a drastic oversimplification of the logic needed to reverse the width calculation
                        // in FullCalendar AgendaEventRenderer.js renderSlotSegs, but it does well for single events
                        // and doesn't hurt overlapping events.
                        var $ele = $(element);
                        var narrow = $ele.width();
                        var width;
                        if (view.name == "agendaWeek") {
                            width = Math.max(narrow + 6, narrow / .95);
                            $ele.hasClass("pendingEvent") ? width : width++;
                        }
                        else
                            width = view.getColWidth();

                        $ele.css('width', width);
                        if ($.i18n.prop('default.language.direction') == 'rtl') {
                            var left = $ele.position().left;
                            $ele.css('left', left - 20);
                            $(".fc-agenda-days .fc-col0").css("border-left-width", "");
                        }
                        // Tweaked this for the class schedule tooltip generation.  Not a particularly good way of handling this, but it's better than function duplication...
                        // TODO: //addTooltipForLongFields($ele, $ele.closest('.fc-event').hasClass('errorEvent') ? 'error' : null);

                        scrollToFirstEvent();

                    },
                    eventAfterAllRender: function (v) {
                        $("#calendar table.fc-agenda-days ").attr("dir", $.i18n.prop('default.language.direction') == 'ltr' ? "ltr" : "ltr");
                        if ($.i18n.prop('default.language.direction') == 'rtl') {
                            $(".fc-agenda-gutter.fc-widget-header.fc-last").removeClass("fc-last").addClass("fc-fake-last");
                            $(".fc-agenda-axis.fc-widget-header.fc-first").removeClass("fc-first").addClass("fc-fake-first");
                            $(".fc-agenda-gutter.fc-widget-header.fc-fake-last").removeClass("fc-fake-last").addClass("fc-first-rtl");
                            $(".fc-agenda-axis.fc-widget-header.fc-fake-first").removeClass("fc-fake-first").addClass("fc-last-rtl");
                        }

                        if(scope.hasPrevWeek) {
                            $('.fc-prev-button').removeClass('fc-state-disabled');
                        }
                        else {
                            $('.fc-prev-button').addClass('fc-state-disabled');
                        }

                        if(scope.hasNextWeek) {
                            $('.fc-next-button').removeClass('fc-state-disabled');
                        }
                        else {
                            $('.fc-next-button').addClass('fc-state-disabled');
                        }
                    }
                });

                //$("#calendar div").attr("dir", $.i18n.prop('default.language.direction') == 'ltr' ? "ltr" : "rtl");

            //}
        }
    };
}]);

proxyAppDirectives.directive('textWithLinks', [function () {
    return{
        restrict: 'E',
        template: '<div ng-if=textData>\n' +
            '            <span ng-repeat="textSegment in textData">\n' +
            '                <span ng-if="textSegment.url"><a href="{{textSegment.url}}">{{textSegment.text}}</a></span>\n' +
            '                <span ng-if="!textSegment.url">{{textSegment.text}}</span>\n' +
            '            </span>\n' +
            '      </div>',
        scope: {
            textData: '='
        }
    };
}]);
