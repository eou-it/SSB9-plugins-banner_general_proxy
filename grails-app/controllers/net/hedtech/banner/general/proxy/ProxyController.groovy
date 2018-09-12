/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.student.history.HistoryTermForStudentGrades
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

import net.hedtech.banner.general.PersonalInformationControllerUtility
import net.hedtech.banner.general.system.Term
import net.hedtech.banner.security.XssSanitizer

/**
 * Controller for Proxy
 */
class ProxyController {
    static defaultAction = 'landingPage'

    def generalSsbProxyService
    def personRelatedHoldService
    def termProxyService
    def gradesProxyService
    def awardHistoryProxyService

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
            render view: "/proxy/actionpassword", params: params, model: [token: params.token, gidm : result.gidm]
        }
    }


    def resetPinAction() {

        def result = generalSsbProxyService.savePin(params."gidm", params.p_pin1, params.p_pin2, params.p_email, params.p_pin_orig)

        if(!result.errorStatus) {
            redirect (uri: "/login/auth")
        }else{
            flash.message = result.message
            render view: "/proxy/resetpin",  model: [gidm : result.gidm]
        }
    }


    /**
     * Gets the Holds model for the student
     *
     */
    def getHolds() {
        def result = personRelatedHoldService.getWebDisplayableHolds(XssSanitizer.sanitize(params.pidm));

        render result as JSON
    }

    def getCourseSchedule() {
        def result = generalSsbProxyService.getCourseSchedule(params.pidm, params.date);

        render result as JSON
    }

    def getCourseScheduleDetail() {
        def result = generalSsbProxyService.getCourseScheduleDetail(params.pidm, params.termCode);

        render result as JSON
    }


    /**
     * Gets the list of terms model for student - grades view model
     *
     */
    def getTerms(params) {
        def pidm = session["currentStudentPidm"]?.toInteger()
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render termProxyService.fetchTermList(pidm, map.searchString, map.max,  map.offset) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /**
     * Gets the list of aid years model for student
     *
     */
    def getAidYears(params) {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)
        def aidYears = generalSsbProxyService.fetchAidYearList(map.max, map.offset, map.searchString)
        try {
            render aidYears as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /**
     * Sets the current student pidm
     *
     */
    def setPidm(params){
        def pidm =XssSanitizer.sanitize(params.pidm)
        session["currentStudentPidm"] = pidm
        render "PIDM context set"
    }

    /**
     * Gets the grades model for the student
     *
     */
    def getGrades(){
        try {
            render gradesProxyService.viewGrades(params)
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /**
     * Gets the financial aid status model for the student
     *
     */
    def getFinancialAidStatus(){
        def result = generalSsbProxyService.getFinancialAidStatus(params.pidm, params.aidYear)

        render result as JSON
    }

    def getAwardPackage() {
        def result = generalSsbProxyService.getAwardPackage(params.pidm, params.aidYear);

        render result as JSON
    }


    /**
     * Gets Financial Aid Award History model for the student
     *
     */
    def getAwardHistory() {
        def result = awardHistoryProxyService.getAwardHistory(XssSanitizer.sanitize(params.pidm));
        render result as JSON
    }

    def getAccountSummary() {
        def result = generalSsbProxyService.getAccountSummary(params.pidm);

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
