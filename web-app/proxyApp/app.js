/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
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
                $rootScope.$on('$stateChangeStart',
                    function(event, toState, toParams, fromState, fromParams, options) {
                        if(!['/home', '/proxypersonalinformation'].includes(toState.url)) {
                            proxyAppService.checkStudentPageForAccess({id: sessionStorage.getItem("id"), name: toState.name}).$promise.then(function(response) {

                                if (response.failure && !response.authorized) {
                                    notificationCenterService.clearNotifications();
                                    notificationCenterService.addNotification(response.message, "error", true);
                                    event.preventDefault();
                                    // transitionTo() promise will be rejected with
                                    // a 'transition prevented' error

                                    if (!response.authorized) {
                                        $state.go('home',
                                            {reload: true, inherit: false, notify: true}
                                        );
                                    }
                                }
                            });
                        }
                    });

                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
                $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                    $state.previous = fromState;
                    $state.previousParams = fromParams;
                    breadcrumbService.setBreadcrumbs(toState.data.breadcrumbs);
                    breadcrumbService.refreshBreadcrumbs();
                });

                $(document.body).removeAttr("role");
                $("html").attr("dir", $filter('i18n')('default.language.direction') === 'ltr' ? "ltr" : "rtl");
                $rootScope.notificationErrorType = "error";
                $rootScope.notificationSuccessType = "success";
                $rootScope.notificationWarningType = "warning";
                $rootScope.flashNotification = true;
                //IE fix
                if (!window.location.origin) {
                    window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
                }

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
            }
        ]
    );

proxyApp.constant('webAppResourcePathString', '../plugins/banner-general-proxy-0.1');


proxyApp.config(function ($stateProvider, $urlRouterProvider, webAppResourcePathString) {
    // For any unmatched url, send to landing page
    var url = url ? url : 'home';

    $urlRouterProvider.otherwise(url);

    /*************************************************************************
     * Defining all the different states of the proxyApp landing pages. *
     *************************************************************************/
    $stateProvider
        .state('home', {
            url: "/home",
            templateUrl: webAppResourcePathString + '/proxyApp/proxyAccessHome/proxyLandingPage.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/proxyPersonalInfo/proxyPersonalInformation.html',
            controller: 'proxyPersonalInformationController',
            resolve: {
            },
            data: {
                breadcrumbs: [{label: 'proxy.profile.heading'}]
            }
        })
        .state('/ssb/proxy/holds', {
            url: "/viewHolds",
            templateUrl: webAppResourcePathString + '/proxyApp/student/holds.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/student/grades.html',
            controller: 'proxyViewGradesController',
            resolve: {
            },
            data: {
                breadcrumbs: [{label: 'proxy.grades.heading'}]
            }

        })
        .state('/ssb/proxy/finaidappsumm', {
            url: "/financialaidstatus",
            templateUrl: webAppResourcePathString + '/proxyApp/finaid/finaidStatus.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/student/courseSchedule.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/student/courseScheduleDetail.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/finaid/awardPackage.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/finaid/awardHistory.html',
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
            templateUrl: webAppResourcePathString + '/proxyApp/student/accountSummary.html',
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
});

proxyApp.config(['$locationProvider',
    function ($locationProvider) {
        $locationProvider.html5Mode(false);
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
        $httpProvider.interceptors.push(function ($q, $window, $rootScope) {
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
        });
    }
]);
