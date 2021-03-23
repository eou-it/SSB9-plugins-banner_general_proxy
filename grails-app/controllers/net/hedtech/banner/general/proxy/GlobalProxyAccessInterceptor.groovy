/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

class GlobalProxyAccessInterceptor {

    def springSecurityService
    def generalSsbProxyService


    int order = HIGHEST_PRECEDENCE + 300

    GlobalProxyAccessInterceptor() {
        match controller: 'studentProfile', action: '*'
        match controller: 'studentGrades', action: '*'
        match controller: 'studentTaxNotification', action: '*'
        match controller: 'awardHistory', action: '*'
        match controller: 'awardOffer' , action: '*'
        match controller: 'studentAcademicProgress', action: '*'
        match controller: 'federalShoppingSheet', action: '*'
        match controller: 'resources', action: '*'
    }


    def getGlobalProxyURLConfig() {
        ["resources"              : "resources",
         "studentTaxNotification" : "studentTaxNotification",
         "studentProfile"         : "studentProfile",
         "studentGrades"          : "studentGrades",
         "awardHistory"           : "listAwardHistoryDetails",
         "awardLetter"            : "listAwardLetterDetails",
         "studentAcademicProgress": "studentAcademicProgress",
         "federalShoppingSheet"   : "federalShoppingSheet",
         "studentNotifications"   : "notifications"
        ]

    }


    boolean before() {

        if (session["globalProxyMode"]) {

            def userPidm = springSecurityService?.getAuthentication()?.user?.pidm
            def p_proxyIDM = generalSsbProxyService.getGIDMfromPidmGlobalAccess(session["loggedUserPidm"])


            def pages = generalSsbProxyService.getProxyPages(p_proxyIDM, userPidm)


            def page = getGlobalProxyURLConfig()."$controllerName"


            if (page && !pages?.pages?.find { it?.url?.contains(page) }) {
                response.sendError(403)
                return
            }
        }

        return true
    }

    boolean after() { true }

    void afterView() {

        // no-op
    }

    private String constructUrl(request, controllerName, actionName) {
        String theUrl = "/ssb/"

        if (controllerName) {
            theUrl += controllerName
            theUrl += actionName ? "/" + actionName : ""
            //theUrl += request.getQueryString() ? "?" + request.getQueryString(): ""
            def requestParams = request.getParameterMap()
            request.getSession().setAttribute("SS_ROLE_SELECTION_PARAM_MAP", requestParams)
        }
        log.debug "StudentPageAccessFilters: construct url ${theUrl}"
        return theUrl
    }

}