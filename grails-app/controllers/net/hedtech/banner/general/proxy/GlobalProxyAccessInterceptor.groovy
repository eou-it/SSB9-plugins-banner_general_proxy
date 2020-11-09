/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy
import net.hedtech.banner.security.XssSanitizer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

class GlobalProxyAccessInterceptor {

    def springSecurityService
    def generalSsbProxyService


    int order = HIGHEST_PRECEDENCE + 300

    GlobalProxyAccessInterceptor() {
        match controller: '*', action: '*'
    }


    def getGlobalProxyConfig() {
        ["/ssb/studentTaxNotification" : ["/ssb/studentTaxNotification",
                                          "/ssb/studentTaxNotification/getTaxYears",
                                          "/ssb/accountSummary/getDefaultConfiguration",
                                          "/ssb/login/auth",
                                          "/ssb/studentHold/getHoldCount",
                                          "/ssb/selfServiceMenu/data?type=Personal",
                                          "/ssb/selfServiceMenu/data","/ssb/logout",
                                          "/ssb/studentTaxNotification/getTaxNotificationAndConfiguration"
        ],
          "/ssb/studentProfile" :         ["/ssb/studentProfile",
                                           "/ssb/studentPicture/picture",
                                           "ssb/studentProfile/renderCurriculumTemplate",
                                           "ssb/studentProfile/viewRegistrationNotices",
                                           "/ssb/studentProfile/viewRegistrationNotices",
                                           "/ssb/studentNotes/getConfiguration",
                                           "/ssb/studentHolds/getHoldsCountCacheHolds",
                                           "/ssb/studentProfile/viewRegisteredCourseList",
                                           "/ssb/studentProfile/viewGPAHoursList"
          ]
                ]
    }


    boolean before() {

        String theUrl = constructUrl(request, controllerName, actionName)

        println "URL: " + theUrl
        println "CONTROLLER-NAME: " + controllerName
        def requestParams = request.getParameterMap()
        def pageUrl = requestParams.url
        def studentPidm = requestParams.pidm

        if (session["globalProxyMode"]) {
            if (!theUrl.contains("proxy") || !theUrl.contains("logout")) {
                def x = getGlobalProxyConfig().collectMany { k, v -> (v.contains(theUrl)) ? [k] : [] }
                println "x1->: " + x[0]
                if(x){
                    println "CHECK-ACCESS"

                    println "LOGGED-USER-PIDM " + session["loggedUserPidm"]
                    def userPidm = springSecurityService?.getAuthentication()?.user?.pidm
                    def p_proxyIDM = generalSsbProxyService.getGIDMfromPidmGlobalAccess(session["loggedUserPidm"])
                    println "USER-PIDM: " + userPidm
                    println "USER-GIDM: " + p_proxyIDM
                    println "URL-PAGE: " + theUrl

                   /*
                    def pages = generalSsbProxyService.getProxyPages(p_proxyIDM,userPidm)
                    println "PAGES: " + pages

                    if (!pages.pages.find{it.url == x[0]}){
                        println "****ERROR*****"
                        //log.error('Invalid attempt for Id: ' + id)
                        //403
                        redirect(controller: "error", action: "accessForbidden")
                        return false
                    }
                    */
                }else{
                    //403
                    //redirect(controller: "error", action: "accessForbidden")
                    //return false
                }
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

        if(controllerName) {
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
