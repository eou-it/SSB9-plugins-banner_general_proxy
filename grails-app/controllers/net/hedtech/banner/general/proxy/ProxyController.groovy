/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Controller for Proxy
 */
class ProxyController {
    static defaultAction = 'landingPage'

    def generalSsbProxyService
    def personRelatedHoldService

    def landingPage() {
        try {

            def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

            if(p_proxyIDM){
                generalSsbProxyService.updateProxyHistoryOnLogin()
            }
            render view: "proxy"
        }
        catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }
    }


    def updateProxyProfile(){
        def proxyProfiles =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        try {
            flash.message = null
            generalSsbProxyService.updateProxyProfile(params)
            proxyProfiles =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

            render view: "/proxy/proxypersonalinformation",  model :  [proxyProfile: proxyProfiles.proxyProfile, proxyUiRules : proxyProfiles.proxyUiRules ]
        }catch(Exception e){
            flash.message = e.message
            proxyProfiles.proxyProfile = params
            render view: "/proxy/proxypersonalinformation",  model :  [proxyProfile: proxyProfiles.proxyProfile, proxyUiRules : proxyProfiles.proxyUiRules ]
        }

    }


    def getProxypersonalinformation() {
        def proxyProfile
        proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        render proxyProfile as JSON
    }


    def updateProxypersonalinformation() {
        def updatedProfile = fixJSONObjectForCast(request?.JSON ?: params)
        try {
            generalSsbProxyService.updateProxyProfile(updatedProfile)

            Map response = [failure: false]
            render response as JSON
        }
        catch(Exception e){
            def response = [message: e.message, failure: true]
            render response as JSON
        }

    }


    def getStudentListForProxy(){
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        render generalSsbProxyService.getStudentListForProxy(p_proxyIDM) as JSON
    }


    def proxyAction(){

        def result = generalSsbProxyService.setProxy(params.p_token)

        if (result.verify) {

            render view: "actionpassword", params: params, model: [token: params.p_token, gidm : result.gidm]

        }
        else if (result.login || result.error) {
            String messageText = result.message
            if(result.message.equals('token-expire')) {
                messageText = MessageHelper.message('proxy.error.tokenExpired')
            }

            flash.message = messageText
            forward controller: "login", action: "auth", params: params
        }
    }


    def submitActionPassword() {
        flash.message = ""

        def result = generalSsbProxyService.setProxyVerify(params.token, params.p_verify, params.gidm)

        if (result.doPin) {
            render view: "/proxy/resetpin", model: [gidm : result.gidm]
        } else {
            flash.message = message( code:"proxy.actionpassword.invalid" )
            render view: "/proxy/actionpassword", params: params, model: [token: params.p_token, gidm : result.gidm]
        }
    }


    def resetPinAction() {

        def result = generalSsbProxyService.savePin(params."gidm", params.p_pin1, params.p_pin2, params.p_email, params.p_pin_orig)

        if(!result.errorStatus) {
            redirect (uri: "/login/auth")
        }else{
            flash.message = message( code: "proxy.pinmanagement.invalid." + result.error )
            render view: "/proxy/resetpin"
        }
    }


    def getHolds() {
        def result = personRelatedHoldService.getWebDisplayableHolds(params.pidm);

        render result as JSON
    }


    private def fixJSONObjectForCast(JSONObject json) {
        json.each {entry ->
            // Make JSONObject.NULL a real Java null
            if (entry.value == JSONObject.NULL) {
                entry.value = null

//            If we ever want to fix dates, this is one possible solution
//            } else if (entry.key == "lastModified") {
//                // Make this date string a real Date object
//                entry.value = DateUtility.parseDateString(entry.value, "yyyy-MM-dd'T'HH:mm:ss'Z'")
            }
        }
    }
}