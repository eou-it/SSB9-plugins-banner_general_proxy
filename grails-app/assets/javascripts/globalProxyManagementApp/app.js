/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
var globalProxyMgmtAppControllers = angular.module('globalProxyMgmtAppControllers', []);
var globalProxyMgmtAppDirectives = angular.module('globalProxyMgmtAppDirectives', []);


var globalProxyManagementApp = angular.module('globalProxyManagementApp', [
    'ngResource',
    'ui.router',
    'globalProxyMgmtAppControllers',
    'globalProxyMgmtAppDirectives',
    'ui.bootstrap',
    'I18n',
    'datePickerApp',
    'xe-ui-components',
    'ui.select'])
    .run(
        ['$rootScope', '$state', '$stateParams', '$filter', 'breadcrumbService', 'notificationCenterService',
            function ($rootScope, $state, $stateParams, $filter, breadcrumbService, notificationCenterService) {
                $rootScope.notificationErrorType = "error";
                $rootScope.notificationSuccessType = "success";
                $rootScope.notificationWarningType = "warning";
                $rootScope.notificationInfoType = "info";
                $rootScope.flashNotification = true;

                $rootScope.$on('$stateChangeStart',
                    function(event, toState, toParams, fromState, fromParams, options) {
                        // Prevent notifications from a previous page from displaying
                        notificationCenterService.clearNotifications();
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

globalProxyManagementApp.constant('webAppResourcePathString', '../assets');


globalProxyManagementApp.config(['$stateProvider', '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {
        // For any unmatched url, send to landing page
        var url = url ? url : 'home';

        $urlRouterProvider.otherwise(url);

        /*************************************************************************
         * Defining all the different states of the globalProxyManagementApp landing pages. *
         * globalProxyMgmtEditProxyDesktop.html
         *************************************************************************/
        $stateProvider
            .state('home', {
                url: "/home",
                templateUrl: '../assets/globalProxyManagementApp/globalProxyManagementHome/globalProxyMgmtMain.html',
                controller: 'globalProxyMgmtMainController',
                //resolve: {
                //TODO add resolve if needed
                //    proxyConfigResolve: ['globalProxyMgmtAppService', function (globalProxyMgmtAppService) {
                //        return globalProxyMgmtAppService.getFromPersonalInfo('ProxyConfig').$promise;
                //    }]
                //},
                data: {
                    breadcrumbs: []
                },
                params: {
                    id: null,
                    onLoadNotifications: []
                }
            })
            .state('editProxy', {
                url: '/editProxy/?alt&cver&firstName&lastName&email',
                templateUrl: '../assets/globalProxyManagementApp/globalProxyManagementEditProxy/globalProxyMgmtEditProxy.html',
                controller: 'globalProxyMgmtEditProxyController',
                //resolve: {
                //TODO Add resolve if needed
                //    proxyConfigResolve: ['globalProxyMgmtAppService', function (globalProxyMgmtAppService) {
                 //       return globalProxyMgmtAppService.getFromPersonalInfo('ProxyConfig').$promise;
                 //   }]
                //},
                data: {
                    breadcrumbs: [{label: 'proxyManagement.label.addProxy'}]
                },
                params: {
                    id: null
                }
            });
    }
]);

globalProxyManagementApp.config(['$locationProvider',
    function ($locationProvider) {
        $locationProvider.html5Mode(false);
        $locationProvider.hashPrefix('');
    }
]);

globalProxyManagementApp.config(['$httpProvider',
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
