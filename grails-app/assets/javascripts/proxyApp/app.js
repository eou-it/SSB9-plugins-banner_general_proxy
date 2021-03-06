/*******************************************************************************
 Copyright 2019-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
var proxyAppControllers = angular.module('proxyAppControllers', []);
var proxyAppDirectives = angular.module('proxyAppDirectives', []);


var proxyApp = angular.module('proxyApp', [
    'ngResource',
    'ui.router',
    'proxyAppControllers',
    'proxyAppDirectives',
    'ui.bootstrap',
    'I18n',
    'datePickerApp',
    'xe-ui-components',
    'ui.select'])
    .run(
        ['$rootScope', '$state', '$stateParams', '$filter', 'proxyAppService', 'breadcrumbService', 'notificationCenterService',
            function ($rootScope, $state, $stateParams, $filter, proxyAppService, breadcrumbService, notificationCenterService) {
                $rootScope.notificationErrorType = "error";
                $rootScope.notificationSuccessType = "success";
                $rootScope.notificationWarningType = "warning";
                $rootScope.flashNotification = true;

                $rootScope.$on('$stateChangeStart',
                    function(event, toState, toParams, fromState, fromParams, options) {
                        // Prevent notifications from a previous page from displaying
                        notificationCenterService.clearNotifications();


                        //Logs the History for the Proxy Page Access
                        if ( typeof toState.data.breadcrumbs[0] !== "undefined") {
                            proxyAppService.getProxyConfig().$promise.then(function (config) {
                                if (config.pageDisplayInHistory) {
                                    proxyAppService.updateProxyHistoryOnPageAccess((toState.data.breadcrumbs[0].label));
                                }
                            });
                        }

                        if(toState.url !== '/home' && toState.url !== '/proxypersonalinformation') {
                            proxyAppService.checkStudentPageForAccess({id: sessionStorage.getItem("id"), name: toState.name}).$promise.then(function(response) {
                                var notifications = [];

                                if (response.failure && !response.authorized) {
                                    notifications.push({message: response.message,
                                                        messageType: $rootScope.notificationErrorType,
                                                        flashType: $rootScope.flashNotification});

                                    event.preventDefault();
                                    // transitionTo() promise will be rejected with
                                    // a 'transition prevented' error

                                    $state.go('home',
                                        {onLoadNotifications: notifications},
                                        {reload: true, inherit: false, notify: true}
                                    );
                                }
                            });
                        }
                    });

                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
                $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                    var contextualizeBreadcrumbs = function() {
                        // Set up breadcrumbs to display correctly in various contexts, e.g. when navigating from
                        // "Course Schedule" to "Course Schedule Details" or from "Financial Aid Application Summary
                        // Status" to "Award Package".

                        var breadcrumbs = [],
                            finaidDests = ['/ssb/proxy/awardPackage', '/ssb/proxy/awardhist', '/ssb/proxy/acctsumm'];

                        if ( (fromState.name === '/ssb/proxy/crsesched' && toState.name === '/ssb/proxy/courseScheduleDetail') ||
                             (fromState.name === '/ssb/proxy/finaidappsumm' && _.contains(finaidDests, toState.name))) {
                            breadcrumbs.push.apply(breadcrumbs, fromState.data.breadcrumbs);

                            // If any breadcrumbs were indeed pushed, set the URL of the "from state" to be the URL for
                            // the last breadcrumb. (In the current implementation, there will never be more than one
                            // breadcrumb here anyway, but if any nodes are added in the future, the last -- read most
                            // recent -- breadcrumb should be that of the "from state.")
                            if (breadcrumbs) {
                                breadcrumbs[breadcrumbs.length - 1].url = fromState.url;
                            }
                        }

                        breadcrumbs.push.apply(breadcrumbs, toState.data.breadcrumbs);

                        return breadcrumbs;
                    };

                    $state.previous = fromState;
                    $state.previousParams = fromParams;
                    breadcrumbService.setBreadcrumbs(contextualizeBreadcrumbs());
                    breadcrumbService.refreshBreadcrumbs();
                });

                $(document.body).removeAttr("role");
                $("html").attr("dir", $filter('i18n')('default.language.direction') === 'ltr' ? "ltr" : "rtl");

                //IE fix
                if (!window.location.origin) {
                    window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
                }

                $rootScope.isRTL = $('meta[name=dir]').attr("content") === 'rtl';

                $rootScope.isDesktopView = isDesktop();

                // Above, we use the isDesktop function implemented in the banner_ui_ss plugin, which thus far has
                // proven to be satisfactory.  Below we modify the implementation of isTablet from banner_ui_ss to
                // be consistent with the definition of "is tablet" elsewhere in this app.
                var isTablet = window.matchMedia("only screen and (min-width: 768px) and (max-width:1024px)");
                $rootScope.isTabletView = isTablet.matches;

                $rootScope.isMobileView = function() {
                    var isMobile = window.matchMedia("only screen and (min-width: 0px) and (max-width: 767px)");
                    return isMobile.matches;
                };

                $rootScope.isAndroid = (/(android)/i.test(navigator.userAgent));

                if ($rootScope.isAndroid) {
                    $("html").addClass('device-android');
                }

                $rootScope.playAudibleMessage = null;

                $rootScope.applicationContextRoot = $('meta[name=applicationContextRoot]').attr("content");

                $rootScope.profileRequired = ('true' === $('meta[name=proxyProfile]').attr("content"));

                _.extend($.i18n.map, window.i18n); //merge i18ns b/c xe-components use different i18n message object
            }
        ]
    );

proxyApp.constant('webAppResourcePathString', '../assets');


proxyApp.config(['$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {
        // For any unmatched url, send to landing page
        var url = url ? url : 'home';

        $urlRouterProvider.otherwise(url);

        /*************************************************************************
         * Defining all the different states of the proxyApp landing pages. *
         *************************************************************************/
        $stateProvider
            .state('home', {
                url: "/home",
                templateUrl: '../assets/proxyApp/proxyAccessHome/proxyLandingPage.html',
                controller: 'proxyLandingPageController',
                resolve: {
                },
                data: {
                    breadcrumbs: []
                },
                params: {
                    onLoadNotifications: []
                }
            })
            .state('proxyPersonalInfo', {
                url: "/proxypersonalinformation",
                templateUrl: '../assets/proxyApp/proxyPersonalInfo/proxyPersonalInformation.html',
                controller: 'proxyPersonalInformationController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.profile.heading'}]
                }
            })
            .state('/ssb/proxy/holds', {
                url: "/viewHolds",
                templateUrl: '../assets/proxyApp/student/holds.html',
                controller: 'proxyViewHoldsController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.holds.heading'}]
                },
                params: {
                    id: null
                }

            })
            .state('/ssb/proxy/grades', {
                url: "/viewGrades",
                templateUrl: '../assets/proxyApp/student/grades.html',
                controller: 'proxyViewGradesController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.grades.heading'}]
                }

            })
            .state('/ssb/proxy/finaidappsumm', {
                url: "/financialaidstatus",
                templateUrl: '../assets/proxyApp/finaid/finaidStatus.html',
                controller: 'proxyViewFinaidStatusController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.finaid.status.heading'}]
                },
                params: {
                    id: null
                }
            })
            .state('/ssb/proxy/crsesched', {
                url: "/courseSchedule",
                templateUrl: '../assets/proxyApp/student/courseSchedule.html',
                controller: 'proxyViewCourseSchedController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.schedule.heading'}]
                },
                params: {
                    id: null
                }
            })
            .state('/ssb/proxy/courseScheduleDetail',{
                url: "/courseScheduleDetail",
                templateUrl: '../assets/proxyApp/student/courseScheduleDetail.html',
                controller: 'proxyCourseSchedDetails',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.scheduleDetails.heading'}]
                },
                params: {
                    id: null,
                    crn: null,
                    termCode: null,
                    termDesc: null
                }
            })
            .state('/ssb/proxy/awardPackage',{
                url: "/awardPackage",
                templateUrl: '../assets/proxyApp/finaid/awardPackage.html',
                controller: 'proxyAwardPackage',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.awardPackage.heading'}]
                },
                params: {
                    id: null
                }
            })
            .state('/ssb/proxy/awardhist', {
                url: "/awardHistory",
                templateUrl: '../assets/proxyApp/finaid/awardHistory.html',
                controller: 'proxyAwardHistoryController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.awardHistory.heading'}]
                },
                params: {
                    id: null
                }
            })
            .state('/ssb/proxy/acctsumm', {
                url: "/accountSummary",
                templateUrl: '../assets/proxyApp/student/accountSummary.html',
                controller: 'proxyAccountSummaryController',
                resolve: {
                },
                data: {
                    breadcrumbs: [{label: 'proxy.acctSummary.heading'}]
                },
                params: {
                    id: null
                }
            });
    }
]);

proxyApp.config(['$locationProvider',
    function ($locationProvider) {
        $locationProvider.html5Mode(false);
        $locationProvider.hashPrefix('');
    }
]);

proxyApp.config(['$httpProvider',
    function ($httpProvider) {
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
        $httpProvider.defaults.cache = false;
        $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';
        $httpProvider.interceptors.push(['$q', '$window','$rootScope', function ($q, $window, $rootScope) {
            $rootScope.ActiveAjaxConectionsWithouthNotifications = 0;
            var checker = function (parameters, status) {
                //YOU CAN USE parameters.url TO IGNORE SOME URL
                if (status === "request") {
                    $rootScope.ActiveAjaxConectionsWithouthNotifications += 1;
                    $('.body-overlay').addClass('loading');
                    $("#content").attr('aria-busy', true);
                }
                if (status === "response") {
                    $rootScope.ActiveAjaxConectionsWithouthNotifications -= 1;

                }
                if ($rootScope.ActiveAjaxConectionsWithouthNotifications <= 0) {
                    $rootScope.ActiveAjaxConectionsWithouthNotifications = 0;
                    $('.body-overlay').removeClass('loading');
                    $("#content").attr('aria-busy', false);
                }
            };
            return {
                'request': function (config) {
                    checker(config, "request");
                    return config;
                },
                'requestError': function (rejection) {
                    checker(rejection.config, "request");
                    return $q.reject(rejection);
                },
                'response': function (response) {
                    checker(response.config, "response");
                    return response;
                },
                'responseError': function (rejection) {
                    checker(rejection.config, "response");
                    if (rejection.status === 403) {
                        window.location.href = '/login/denied';
                    }
                    return $q.reject(rejection);
                }
            };
        }]);
    }
]);
