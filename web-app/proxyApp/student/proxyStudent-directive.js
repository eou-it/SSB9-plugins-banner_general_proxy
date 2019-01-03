/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyAppDirectives.directive('fullCalendar',['proxyAppService', '$filter', '$compile', '$rootScope', function(proxyAppService, $filter, $compile, $rootScope) {

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
        },
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
        };

    return {
        link: function(scope, elem, attrs) {
            var isMobile = $rootScope.isMobileView(),
                isRTL = $.i18n.prop('default.language.direction') === 'rtl',
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
                    slotLabelFormat: 'h:mm a',
                    scrollTime: '08:00:00',
                    columnHeaderFormat: 'DD ddd',
                    timeFormat: 'h:mm', // 5:00 - 6:30,
                    titleFormat: '['+ weekOfText +'] MMMM DD, YYYY',
                    firstDay: 1,
                    isRTL: isRTL,
                    dayNames: $.i18n.prop("default.gregorian.dayNames").split(','),
                    dayNamesShort: $.i18n.prop("default.gregorian.dayNamesShort").split(','),
                    monthNames: $.i18n.prop("default.gregorian.monthNames").split(','),
                    monthNamesShort: $.i18n.prop("default.gregorian.monthNamesShort").split(','),
                    axisFormat: $.i18n.prop('events.row.time.format'),
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
