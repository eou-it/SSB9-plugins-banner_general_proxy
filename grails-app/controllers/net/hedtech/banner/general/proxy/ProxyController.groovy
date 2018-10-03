/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder
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
    def proxyFinAidService
    def proxyConfigurationService
    def directDepositAccountCompositeService

    def landingPage() {
        try {

            def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm

            if(p_proxyIDM){
                generalSsbProxyService.updateProxyHistoryOnLogin()
            }

            def profileRequired = false

            // check if proxy profile data is not complete
            // forces to pass profileRequired = true
            if (generalSsbProxyService.isRequiredDataForProxyProfileComplete(p_proxyIDM)?.trim()) {
                profileRequired = true
            }

            render view: "proxy", model : [proxyProfile: profileRequired]

        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
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
            flash.message = message( code:"proxy.passwordManagement.invalidPassord" )
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
        result.rows?.each {
            def amountTxt = '-'
            if(it.r_amount_owed && it.r_amount_owed != 0) {
                amountTxt = directDepositAccountCompositeService.formatCurrency(it.r_amount_owed)
            }
            it.r_amount_owed = amountTxt
        }

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
        def map = ProxyControllerUtility.getFetchListParams(params)

        try {
            render termProxyService.fetchTermList(pidm, map.searchString, map.max,  map.offset) as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /**
     * Gets the list of aid years model for student
     *
     */
    def getAidYears(params) {
        def map = ProxyControllerUtility.getFetchListParams(params)
        def aidYears = proxyFinAidService.fetchAidYearList(map.max, map.offset, map.searchString)
        try {
            render aidYears as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
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
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /**
     * Gets the financial aid status model for the student
     *
     */
    def getFinancialAidStatus() {
        def result = generalSsbProxyService.getFinancialAidStatus(params.pidm, params.aidYear)
        result.awardPackage?.each {
            if(it.amount != null) {
                it.text = it.text + directDepositAccountCompositeService.formatCurrency(it.amount) + '.'
            }
        }
        result.costOfAttendance?.each {
            if(it.amount != null) {
                it.text = it.text + directDepositAccountCompositeService.formatCurrency(it.amount) + '.'
            }
        }

        render result as JSON
    }

    def getAwardPackage() {
        def result = proxyFinAidService.getAwardPackage(params.pidm, params.aidYear);
        result.needsCalc?.attendanceCost = directDepositAccountCompositeService.formatCurrency(result.needsCalc?.attendanceCost)
        result.needsCalc?.familyContrib = directDepositAccountCompositeService.formatCurrency(result.needsCalc?.familyContrib)
        result.needsCalc?.initialNeed = directDepositAccountCompositeService.formatCurrency(result.needsCalc?.initialNeed)
        result.needsCalc?.need = directDepositAccountCompositeService.formatCurrency(result.needsCalc?.need)
        result.needsCalc?.outsideResrc = directDepositAccountCompositeService.formatCurrency(result.needsCalc?.outsideResrc)

        result.costOfAttendance?.budgets?.each {
            it.amount = directDepositAccountCompositeService.formatCurrency(it.amount)
        }
        result.costOfAttendance?.totalTxt = directDepositAccountCompositeService.formatCurrency(result.costOfAttendance?.total)

        result.loanInfo?.subsidized = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.subsidized)
        result.loanInfo?.unsubsidized = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.unsubsidized)
        result.loanInfo?.gradPlus = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.gradPlus)
        result.loanInfo?.parentPlus = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.parentPlus)
        result.loanInfo?.perkins = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.perkins)
        result.loanInfo?.directUnsub = directDepositAccountCompositeService.formatCurrency(result.loanInfo?.directUnsub)

        result.awardInfo?.aidYearAwards?.aidAwards?.each {
            it.acceptAmt = formatCurrencyDashZeroes(it.acceptAmt)
            it.amount = formatCurrencyDashZeroes(it.amount)
            it.cancelAmt = formatCurrencyDashZeroes(it.cancelAmt)
            it.declineAmt = formatCurrencyDashZeroes(it.declineAmt)
            it.offerAmt = formatCurrencyDashZeroes(it.offerAmt)
        }
        result.awardInfo?.aidYearAwards?.totalAcceptAmtTxt = directDepositAccountCompositeService.formatCurrency(result.awardInfo?.aidYearAwards?.totalAcceptAmt)
        result.awardInfo?.aidYearAwards?.totalAmtTxt = directDepositAccountCompositeService.formatCurrency(result.awardInfo?.aidYearAwards?.totalAmt)
        result.awardInfo?.aidYearAwards?.totalCancelAmtTxt = directDepositAccountCompositeService.formatCurrency(result.awardInfo?.aidYearAwards?.totalCancelAmt)
        result.awardInfo?.aidYearAwards?.totalDeclineAmtTxt = directDepositAccountCompositeService.formatCurrency(result.awardInfo?.aidYearAwards?.totalDeclineAmt)
        result.awardInfo?.aidYearAwards?.totalOfferAmtTxt = directDepositAccountCompositeService.formatCurrency(result.awardInfo?.aidYearAwards?.totalOfferAmt)

        result.periodInfo?.periods?.each {
            it.periodAwards.each {
                it.amount = formatCurrencyDashZeroes(it.amount)
            }
            it.totalTxt = directDepositAccountCompositeService.formatCurrency(it.total)
        }

        render result as JSON
    }


    /**
     * Gets Financial Aid Award History model for the student
     *
     */
    def getAwardHistory() {
        def result = proxyFinAidService.getAwardHistory(XssSanitizer.sanitize(params.pidm));
        result.awards?.each {
            it.data?.rows?.each {
                if (it.fund_title.equals('AWARD_TOTAL')) {
                    it.accept_amt = directDepositAccountCompositeService.formatCurrency(it.accept_amt)
                    it.cancel_amt = directDepositAccountCompositeService.formatCurrency(it.cancel_amt)
                    it.decline_amt = directDepositAccountCompositeService.formatCurrency(it.decline_amt)
                    it.offer_amt = directDepositAccountCompositeService.formatCurrency(it.offer_amt)
                } else {
                    it.accept_amt = formatCurrencyDashZeroes(it.accept_amt)
                    it.cancel_amt = formatCurrencyDashZeroes(it.cancel_amt)
                    it.decline_amt = formatCurrencyDashZeroes(it.decline_amt)
                    it.offer_amt = formatCurrencyDashZeroes(it.offer_amt)
                }
                it.paid_amt = directDepositAccountCompositeService.formatCurrency(it.paid_amt)
                it.total_amt = directDepositAccountCompositeService.formatCurrency(it.total_amt)
            }

            it?.resources?.each {
                it.actual_amt = directDepositAccountCompositeService.formatCurrency(it.actual_amt)
                it.est_amt = directDepositAccountCompositeService.formatCurrency(it.est_amt)
            }
        }

        render result as JSON
    }

    def getAccountSummary() {
        def result = generalSsbProxyService.getAccountSummary(params.pidm);
        result.accountBalTxt = directDepositAccountCompositeService.formatCurrency(result.accountBal)
        result.acctTotalTxt = directDepositAccountCompositeService.formatCurrency(result.acctTotal)

        result.terms?.each {
            it.termBalance = directDepositAccountCompositeService.formatCurrency(it.termBalance)
            it.termCharge = directDepositAccountCompositeService.formatCurrency(it.termCharge)
            it.termPay = directDepositAccountCompositeService.formatCurrency(it.termPay)

            it.ledger.each {
                it.balance = formatCurrencyDashZeroes(it.balance)
                it.charge = formatCurrencyDashZeroes(it.charge)
                it.payment = formatCurrencyDashZeroes(it.payment)
            }
        }

        render result as JSON
    }

    private def formatCurrencyDashZeroes(def value) {
        def result = '-'
        if(value != 0) {
            result = directDepositAccountCompositeService.formatCurrency(value)
        }

        return result
    }

    def getConfig() {
        try {
            def map = [:]
            proxyConfigurationService.getProxyParams().each {
                // Web Tailor parameter values cannot be null so will come in here as "<UPDATE ME>" for a "non-value".
                // We change it to null for use in the front end.
                map[it.key] = (it.value == '<UPDATE ME>') ? null : it.value
            }

            render map as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
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
