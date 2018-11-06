/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('fullCalendar',['proxyAppService', '$filter', '$compile', function(proxyAppService, $filter, $compile) {
    function topOf(elements) {
        function topOfOne(element) {
            var pos = $(element).position();
            return pos ? pos.top : null;
        }


        return topOfOne(_.min($(elements), topOfOne));
    }

    var scrollToFirstEvent = _.debounce(function() {
        var top = topOf('.fc-event:visible');
        if (top != null) {
            var scrollable = $('.fc-event').closest('[style*=overflow-y]');
            scrollable.stop().animate({scrollTop:top});
        }
    }, 100);

    var weekOfText = $filter('i18n')('proxy.schedule.weekOf'),
        goToText = $filter('i18n')('proxy.schedule.goTo'),
        datePlaceholderText = $filter('i18n')('default.date.format.watermark'),
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
        };

    return {
        link: function(scope, elem, attrs) {
            //function setupCalendarView() {
            //    var amPm = $.i18n.prop("default.gregorian.amPm").toLowerCase().split(',');
            //    $.fullCalendar.dateFormatters['tt'] = function (d) {
            //        return d.getHours() < 12 ? amPm[0] : amPm[1];
            //    };

            var isMobile = $(window).width() < 768;

                $('#calendar').fullCalendar({
                    customButtons: {
                        nextWeek: {
                            text: '>>',
                            click: function() {
                                var tgtDate = $('#calendar').fullCalendar('getDate').add(7, 'days');
                                $('#calendar').fullCalendar('gotoDate', tgtDate);
                            }
                        },
                        prevWeek: {
                            text: '<<',
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
                    eventTextColor: '#428bca',
                    eventBorderColor: '#2874bb',
                    slotEventOverlap: false,
                    slotDuration: '00:15:00',
                    slotLabelInterval: '01:00',
                    slotLabelFormat: 'h:mm a',
                    scrollTime: '08:00:00',
                    columnHeaderFormat: 'DD ddd',
                    timeFormat: 'h:mm', // 5:00 - 6:30,
                    titleFormat: '['+ weekOfText +'] MMMM DD, YYYY',

                    firstDay: parseInt($.i18n.prop("default.firstDayOfTheWeek")),
                    dayNames: $.i18n.prop("default.gregorian.dayNames").split(','),
                    dayNamesShort: $.i18n.prop("default.gregorian.dayNamesShort").split(','),
                    monthNames: $.i18n.prop("default.gregorian.monthNames").split(','),
                    monthNamesShort: $.i18n.prop("default.gregorian.monthNamesShort").split(','),
                    axisFormat: $.i18n.prop('events.row.time.format'),
                    isRTL: $.i18n.prop('default.language.direction') == 'rtl',
                    events: function (start, end, timezone, callback) {
                        //var events = JSON.parse(sessionStorage.getItem("classScheduleEvents"));
                        //start.add(1, 'days'); //move start to a Monday to coincide with SQL date processing
                        var events;
                        proxyAppService.getCourseSchedule({id: scope.id, date: start.format('MM/DD/YYYY')}).$promise.then(function(response) {
                            events = response.schedule;
                            scope.hasNextWeek = response.hasNextWeek;
                            scope.hasPrevWeek = response.hasPrevWeek;
                            scope.unassignedSchedule = response.unassignedSchedule;
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
                            courseLinkTemplate = html+'<a ui-sref="/ssb/proxy/courseScheduleDetail({id: id})"> '+ event.title +'</a><br><span>'+ event.location +'</span>',
                            courseLinkElem = $compile(courseLinkTemplate)(scope);

                        options.term = event.term;
                        options.courseReferenceNumber = event.crn;
                        options.courseTitle = event.title;

                        $('.fc-title', element).text("");
                        $('.fc-title', element).append(courseLinkElem);
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
                            //width = view.getColWidth();

                        //$ele.css('width', width);
                        if ($.i18n.prop('default.language.direction') == 'rtl') {
                            var left = $ele.position().left;
                            $ele.css('left', left - 20);
                            $(".fc-agenda-days .fc-col0").css("border-left-width", "");
                        }
                        // Tweaked this for the class schedule tooltip generation.  Not a particularly good way of handling this, but it's better than function duplication...
                        // TODO: //addTooltipForLongFields($ele, $ele.closest('.fc-event').hasClass('errorEvent') ? 'error' : null);

                        scrollToFirstEvent();

                    },
                    eventAfterAllRender: function (view) {
                        $("#calendar table.fc-agenda-days ").attr("dir", $.i18n.prop('default.language.direction') == 'ltr' ? "ltr" : "ltr");
                        if ($.i18n.prop('default.language.direction') == 'rtl') {
                            $(".fc-agenda-gutter.fc-widget-header.fc-last").removeClass("fc-last").addClass("fc-fake-last");
                            $(".fc-agenda-axis.fc-widget-header.fc-first").removeClass("fc-first").addClass("fc-fake-first");
                            $(".fc-agenda-gutter.fc-widget-header.fc-fake-last").removeClass("fc-fake-last").addClass("fc-first-rtl");
                            $(".fc-agenda-axis.fc-widget-header.fc-fake-first").removeClass("fc-fake-first").addClass("fc-last-rtl");
                        }

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
                            }
                            backwardElem.addClass('secondary');

                            if(scope.hasNextWeek) {
                                forwardElem.removeClass('fc-state-disabled');
                                forwardElem.removeAttr('disabled');
                            }
                            else {
                                forwardElem.addClass('fc-state-disabled');
                                forwardElem.attr('disabled', '');
                            }
                            forwardElem.addClass('secondary');
                        }

                        if(scope.hasPrevWeek) {
                            prevBtnElem.removeClass('fc-state-disabled');
                            prevBtnElem.removeAttr('disabled');
                        }
                        else {
                            prevBtnElem.addClass('fc-state-disabled');
                            prevBtnElem.attr('disabled', '');
                        }
                        prevBtnElem.addClass('secondary');

                        if(scope.hasNextWeek) {
                            nextBtnElem.removeClass('fc-state-disabled');
                            nextBtnElem.removeAttr('disabled');
                        }
                        else {
                            nextBtnElem.addClass('fc-state-disabled');
                            nextBtnElem.attr('disabled', '');
                        }
                        nextBtnElem.addClass('secondary');
                    },
                    windowResize: function(view) {
                        var datePickerTemplate = null,
                            datePickerElem = null,
                            fc = $('#calendar');
                        if ($(window).width() < 768) {
                            if(view.name === 'agendaSevenDay') {
                                fc.fullCalendar('changeView', 'agendaTwoDay');
                                fc.fullCalendar('option', 'aspectRatio', mobileOptions.aspectRatio);
                                fc.fullCalendar('option', 'header', mobileOptions.header);
                                fc.fullCalendar('option', 'footer', mobileOptions.footer);

                                datePickerTemplate = '<div class="gotodate-block"> <label>'+ goToText +'</label> <input date-picker ng-model="tgtDate" pi-input-watcher on-select="goToDate" class="eds-text-field pi-date-input input-colors" placeholder="'+ datePlaceholderText +'" id="goToDate"/> </div>';
                                datePickerElem = $compile(datePickerTemplate)(scope);
                                $('.fc-header-toolbar > .fc-right').append(datePickerElem);
                            }
                        }
                        else {
                            if(view.name === 'agendaTwoDay') {
                                fc.fullCalendar('changeView', 'agendaSevenDay');
                                fc.fullCalendar('option', 'aspectRatio', desktopOptions.aspectRatio);
                                fc.fullCalendar('option', 'header', desktopOptions.header);
                                fc.fullCalendar('option', 'footer', desktopOptions.footer);

                                datePickerTemplate = '<div class="gotodate-block"> <label>' + goToText + '</label> <input date-picker ng-model="tgtDate" pi-input-watcher on-select="goToDate" class="eds-text-field pi-date-input input-colors" placeholder="' + datePlaceholderText + '" id="goToDate"/> </div>';
                                datePickerElem = $compile(datePickerTemplate)(scope);
                                $('.fc-header-toolbar > .fc-right').append(datePickerElem);
                            }
                        }
                    }
                });

                //add date-picker to calendar
            var datePickerTemplate = '<div class="gotodate-block"> <label>'+ goToText +'</label> <input date-picker ng-model="tgtDate" pi-input-watcher on-select="goToDate" class="eds-text-field pi-date-input input-colors" placeholder="'+ datePlaceholderText +'" id="goToDate"/> </div>',
                datePickerElem = $compile(datePickerTemplate)(scope);
            $('.fc-header-toolbar > .fc-right').append(datePickerElem);


            //$("#calendar div").attr("dir", $.i18n.prop('default.language.direction') == 'ltr' ? "ltr" : "rtl");

            //}
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
