/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy
import net.hedtech.banner.security.XssSanitizer

class GlobalProxyAccessInterceptor {



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

        if (session["globalProxyMode"]) {
            if (!theUrl.contains("proxy") || !theUrl.contains("logout")) {
                def x = getGlobalProxyConfig().collectMany { k, v -> (v.contains(theUrl)) ? [k] : [] }
                println "x1->: " + x
                if(x){
                    println "CHECK-ACCESS"
                }else{
                    //403
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
