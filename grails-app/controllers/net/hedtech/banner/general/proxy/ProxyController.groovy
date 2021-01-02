/*******************************************************************************
 Copyright 2019-2021 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.configuration.ConfigProperties
import net.hedtech.banner.general.system.ProxyAccessSystemOptionType
import net.hedtech.banner.i18n.MessageHelper
import org.grails.web.json.JSONObject
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.security.XssSanitizer
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat

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
    def proxyStudentService
    def proxyConfigurationService
    def currencyFormatHelperService
    def messageSource
    def countyService
    def stateService
    def nationService
    def springSecurityService

    def beforeInterceptor = [action:this.&studentIdCheck]

    private final static String AWARD_PACKAGE_URL = '/ssb/proxy/awardPackage';
    private final static String ACCOUNT_SUMMARY_URL = '/ssb/proxy/acctsumm';
    private final static String AWARD_HISTORY_URL = '/ssb/proxy/awardhist';


    private getAllStudentsInSingleList() {
        def students = []
        students.addAll(session["students"]?.students.active)
        students.addAll(session["students"]?.students.inactive)

        students
    }

    private studentIdCheck() {
        def id = XssSanitizer.sanitize(params.id)
        if (id) {
            def students = session["students"]?.students?.active
            def student = students?.find { it.id == id }
            if (!student) {
                log.error('Invalid attempt for Id: ' + id )
                def response = [message: MessageHelper.message('proxy.error.invalidAttempt')  + id, failure: true]
                render response as JSON
            }
            return true
        }
    }

    private void logProxyNavigationToAccessHistory() {
        def logHistoryMessage = messageSource.getMessage(XssSanitizer?.sanitize("proxy.page.heading" +
                filterFinAidUrl(params?.url)?.replaceAll("/", ".")), null, LocaleContextHolder.getLocale())
        generalSsbProxyService.updateProxyHistoryOnPageAccess(session["currentStudentPidm"], logHistoryMessage)
    }

    // This will cut off the aid year part from url
    private filterFinAidUrl(def url) {
        return (url.contains("financialAid")) ? (url.subSequence(0, url.length() - 5)) : url
    }

    private addFinaidMarkerToUrlIfUrlIsForFinaidPage(paramsUrl) {
        def url
        url = paramsUrl?.indexOf("financialAid") > 0 ? addFinaidMarker(paramsUrl) : paramsUrl
        url
    }

    //Appends characters to URL needed for Financial Aid pages.
    private String addFinaidMarker(String url) {
        def position = url?.indexOf("financialAid")
        return url?.substring(0, url?.indexOf("financialAid") + 12) + "#!" + url?.substring(position + 12)
    }

    private void addProxyFinancialAidConfigurationsToSession() {
        session['proxyWebRules'] = proxyConfigurationService.getFinaidConfigurationsBasedOnRole()
    }

    def onReturn(){
        if (session["loggedUserPidm"]) {
            springSecurityService?.getAuthentication()?.user?.pidm = session["loggedUserPidm"]
            session["globalProxyMode"] = false
            RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("guestUser", false)
            render "context set"
        }else{
            response.sendError( 403 )
            return
        }
    }

    // Main Proxy Page Navigator
    def navigate() {
        try {
            logProxyNavigationToAccessHistory()
            addProxyFinancialAidConfigurationsToSession()
            def url = addFinaidMarkerToUrlIfUrlIsForFinaidPage(params?.url)

            if (params.token) {

                // get the student's  pidm from the token

                def studentPidm = generalSsbProxyService.getStudentPidmFromToken(params.token)
                println "Student-PIDM: " + studentPidm

                if (!studentPidm){
                    response.sendError( 403 )
                    return
                }

                def gidm = generalSsbProxyService.getGIDMfromPidmGlobalAccess(springSecurityService?.getAuthentication()?.user?.pidm)

                def pages = generalSsbProxyService.getProxyPages(gidm,studentPidm)
                def page = url.replace("#","").replace("!","");

                if (url && !pages?.pages?.find{it?.url?.contains(page)}) {
                    log.warn("Access Forbidden: " + page)
                    response.sendError( 403 )
                    return
                }


                session["loggedUserPidm"] = springSecurityService?.getAuthentication()?.user?.pidm
                session["globalProxyMode"] = true
                springSecurityService?.getAuthentication()?.user?.pidm = new Integer(studentPidm)
                RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("guestUser", true)

                session["globalGuestProxyBaseURL"] =  getGSSUrl()
            }

            redirect(url: url, params: params)
        }
        catch (NoSuchMessageException e) {
            log.error(e.toString())
            throw new ApplicationException(ProxyController.class,
                    messageSource.getMessage(XssSanitizer?.sanitize('proxy.error.dataError'), null, LocaleContextHolder.getLocale()))
        }
    }

    /**
     * Returns the general location from gurocfg table
     * @return String
     * */
    private def getGSSUrl() {
        try{
            ConfigProperties configProperties = ConfigProperties.fetchByConfigNameAndAppId('GENERALLOCATION','GENERAL_SS')
            String mepCode = session.getAttribute( 'mep' )
            if (mepCode) {
                log.debug( "mepCode $mepCode" )
                log.debug( "URL to redirect $configProperties.configValue&mepCode=$mepCode" )
                return configProperties? "$configProperties.configValue&mepCode=$mepCode"  : null
            }else{
                log.debug( "URL to redirect $configProperties.configValue" )
                return configProperties? configProperties.configValue  : null
            }
        }catch (SQLException e){
            log.warn("Unable to fetch the configuration GENERALLOCATION "+e.getMessage())
            return ""
        }
    }


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


    def getProxypersonalinformation() {
        def proxyProfile
        proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        render proxyProfile as JSON
    }


    def updateProxypersonalinformation() {
        def updatedProfile = request?.JSON ?: params
        //def updatedProfile = fixJSONObjectForCast(request?.JSON ?: params)
        try {

            Map response = [failure: false, message: generalSsbProxyService.updateProxyProfile(updatedProfile)]
            session["students"] = null
            render response as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
        catch(Exception e){
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }

    }


    /*
      Returns the Student List with a set of authorized pages.
     */
    def getStudentListForProxy(){

        def studentList

        log.debug("Get Student List for Proxy")

        if (session["students"] == null) {
            def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
            studentList = generalSsbProxyService.getStudentListForProxy(p_proxyIDM)
            session["students"] = studentList
        }
        else {
            studentList = session["students"]
        }

        render studentList as JSON
    }


    /*
      Checks the Page for Authorized Access.
     */
    def checkStudentPageForAccess() {

        log.debug("Check Student Page For Access for: " + params.name);

        if (params.name) {
            def page = checkPageForAccess(XssSanitizer.sanitize(params.id), params.name)

            if (page == null) {
                render([failure: true, authorized: false,  message: 'proxy.error.pageAccess'] as JSON)
                return
            }else{
                render([failure: false, authorized: true,  message: 'Access verified'] as JSON)
            }
        }
    }


    def proxyAction(){

        def result = generalSsbProxyService.setProxy(params.p_token)

        if (result.verify) {

            render view: "actionpassword", params: params, model: [token: params.p_token, gidm : result.gidm]

        }
        else if(result.doPin) {
            render view: "/proxy/resetpin", model: [gidm : result.gidm]
        }
        else if (result.login || result.error) {
            if(result.message?.equals('tokenExpired')) {
                flash.message = MessageHelper.message('proxy.error.tokenExpired')
            }
            else if(result.message?.length() > 0) {
                flash.reloginMessage = MessageHelper.message('proxy.message.' + result.message)
            }

            forward controller: "login", action: "auth", params: params
        }
    }


    def submitActionPassword() {
        flash.message = ""

        def result = generalSsbProxyService.setProxyVerify(params.token, params.p_verify, params.gidm)

        if (result.doPin) {
            render view: "/proxy/resetpin", model: [gidm: result.gidm]
        }
        else if (result.login) {
            if(result.message?.equals('tokenExpired')) {
                flash.message = MessageHelper.message('proxy.error.tokenExpired')
            }
            else if(result.message?.length() > 0) {
                flash.reloginMessage = MessageHelper.message('proxy.message.' + result.message)
            }

            forward controller: "login", action: "auth", params: params
        }
        else {
            flash.message = message( code:"proxy.passwordManagement.invalidPassord" )
            render view: "/proxy/actionpassword", params: params, model: [token: params.token, gidm : result.gidm]
        }
    }


    def resetPinAction() {

        def result = generalSsbProxyService.savePin(params."gidm", params.p_pin1, params.p_pin2, params.p_email, params.p_pin_orig)

        if(!result.errorStatus) {
            redirect (uri: "/login/auth")
        }else{
            flash.message = result.message ? result.message : MessageHelper.message('proxy.pinmanagement.invalid.' + result.error)
            render view: "/proxy/resetpin",  model: [gidm : result.gidm]
        }
    }


    /**
     * Gets the Holds model for the student
     *
     */
    def getHolds() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = personRelatedHoldService.getWebDisplayableHolds(pidm);
        result.rows?.each {
            def amountTxt = '-'
            if(it.r_amount_owed && it.r_amount_owed != 0) {
                amountTxt = currencyFormatHelperService.formatCurrency(it.r_amount_owed)
            }
            it.r_amount_owed = amountTxt
        }

        render result as JSON
    }


    /**
     * Gets the View Grades Holds model for the student
     *
     */
    def getViewGradesHolds() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = gradesProxyService.getViewGradesHolds(pidm);

        render result as JSON
    }


    def getCourseSchedule() {
        def id = XssSanitizer.sanitize(params.id)
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = proxyStudentService.getCourseSchedule(pidm, XssSanitizer.sanitize(params.date));
        result.hasDetailAccess = checkPageForAccess(id, '/ssb/proxy/courseScheduleDetail') != null

        render result as JSON
    }

    def getCourseScheduleDetail() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = proxyStudentService.getCourseScheduleDetail(
                pidm,
                XssSanitizer.sanitize(params.termCode),
                XssSanitizer.sanitize(params.crn)
        );


        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        result?.rows?.each{

            it.status_02 = it.status_02 ? df.parse(it.status_02) : it.status_02

            it.tbl_meetings.each{el ->
                el.meet_start = el.meet_start ? df.parse(el.meet_start) : el.meet_start
                el.meet_end = el.meet_end ? df.parse(el.meet_end) : el.meet_end
            }
        }

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
     * Gets the list of registration terms for student
     *
     */
    def getTermsForRegistration() {
        def registrationTerms = [:]
        def pidm = session["currentStudentPidm"]?.toInteger()

        try {
            registrationTerms."terms" = termProxyService.fetchTermList(pidm, "", 10, 0)
            render registrationTerms as JSON
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
    def setId(params){
        def pidm =PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id))).pidm
        springSecurityService?.getAuthentication()?.user?.pidm = pidm
        session["currentStudentPidm"] = pidm
        //session["globalGuestProxyBaseURL"] = "/"
        render "PIDM context set"
    }


    /**
     * @param params Proxy Page Name
     * @return response on history context set
     */
    def updateProxyHistoryOnPageAccess(params){
        def logHistoryMessage = messageSource.getMessage(XssSanitizer.sanitize(params?.label), null, Locale.ENGLISH)
        generalSsbProxyService.updateProxyHistoryOnPageAccess(session["currentStudentPidm"], logHistoryMessage)
        render logHistoryMessage + " on update history context set"
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
        try{
            def id = XssSanitizer.sanitize(params.id)

            def pidm = id ? PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm : session["currentStudentPidm"]

            def result = proxyFinAidService.getFinancialAidStatus(pidm, XssSanitizer.sanitize(params.aidYear))

            if (result.awardPackage) {
                if(result.awardPackage.amount != null) {
                    result.awardPackage.textParams = [currencyFormatHelperService.formatCurrency(result.awardPackage.amount)]
                }

                result.awardPackage.remove('amount')

                if (result.awardPackage.url) {
                    result.awardPackage.hasAccess = checkPageForAccess(id, AWARD_PACKAGE_URL) != null

                    if(result.awardPackage.hasAccess) {
                        result.awardPackage.url = AWARD_PACKAGE_URL
                    }
                    else {
                        result.awardPackage.remove('url')
                    }
                }
            }

            if (result.costOfAttendance) {
                if(result.costOfAttendance.amount != null) {
                    result.costOfAttendance.textParams = [currencyFormatHelperService.formatCurrency(result.costOfAttendance.amount)]
                }

                result.costOfAttendance.remove('amount')
            }

            if(result.financialAidHistory?.url) {
                result.financialAidHistory.hasAccess = checkPageForAccess(id, AWARD_HISTORY_URL) != null

                if(result.financialAidHistory.hasAccess) {
                    result.financialAidHistory.url = AWARD_HISTORY_URL
                }
                else {
                    result.financialAidHistory.remove('url')
                }
            }

            if(result.accountSummary?.url) {
                result.accountSummary.hasAccess = checkPageForAccess(id, ACCOUNT_SUMMARY_URL) != null

                if(result.accountSummary.hasAccess) {
                    result.accountSummary.url = ACCOUNT_SUMMARY_URL
                }
                else {
                    result.accountSummary.remove('url')
                }
            }

            render result as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        } catch (Exception e) {
            log.error(e)
            render([failure: true, authorized: false,  message: MessageHelper.message('proxy.error.dataError')] as JSON)
        }
    }

    def getAwardPackage() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result
        try {

            result = proxyFinAidService.getAwardPackage(pidm, XssSanitizer.sanitize(params.aidYear));
        }
        catch (Exception e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        if(result.hasAwardInfo) {
            if(result.needsCalc?.size()) {
                result.needsCalc.attendanceCost = currencyFormatHelperService.formatCurrency(result.needsCalc.attendanceCost)
                result.needsCalc.familyContrib = currencyFormatHelperService.formatCurrency(result.needsCalc.familyContrib)
                result.needsCalc.initialNeed = currencyFormatHelperService.formatCurrency(result.needsCalc.initialNeed)
                result.needsCalc.need = currencyFormatHelperService.formatCurrency(result.needsCalc.need)
                result.needsCalc.outsideResrc = currencyFormatHelperService.formatCurrency(result.needsCalc.outsideResrc)
            }

            result.costOfAttendance?.budgets?.each {
                it.amount = currencyFormatHelperService.formatCurrency(it.amount)
            }
            result.costOfAttendance?.totalTxt = currencyFormatHelperService.formatCurrency(result.costOfAttendance?.total)

            result.loanInfo = getLoanText(result.loanInfo)

            result.awardInfo.aidYearAwards?.aidAwards?.each {
                it.acceptAmt = formatCurrencyDashZeroes(it.acceptAmt)
                it.amount = formatCurrencyDashZeroes(it.amount)
                it.cancelAmt = formatCurrencyDashZeroes(it.cancelAmt)
                it.declineAmt = formatCurrencyDashZeroes(it.declineAmt)
                it.offerAmt = formatCurrencyDashZeroes(it.offerAmt)
            }
            result.awardInfo.aidYearAwards?.totalAcceptAmtTxt = currencyFormatHelperService.formatCurrency(result.awardInfo.aidYearAwards?.totalAcceptAmt)
            result.awardInfo.aidYearAwards?.totalAmtTxt = currencyFormatHelperService.formatCurrency(result.awardInfo.aidYearAwards?.totalAmt)
            result.awardInfo.aidYearAwards?.totalCancelAmtTxt = currencyFormatHelperService.formatCurrency(result.awardInfo.aidYearAwards?.totalCancelAmt)
            result.awardInfo.aidYearAwards?.totalDeclineAmtTxt = currencyFormatHelperService.formatCurrency(result.awardInfo.aidYearAwards?.totalDeclineAmt)
            result.awardInfo.aidYearAwards?.totalOfferAmtTxt = currencyFormatHelperService.formatCurrency(result.awardInfo.aidYearAwards?.totalOfferAmt)

            result.periodInfo.periods?.each {
                it.periodAwards.each {
                    it.amount = formatCurrencyDashZeroes(it.amount)
                }
                it.totalTxt = currencyFormatHelperService.formatCurrency(it.total)
            }
            result.periodInfo.grandTotal = currencyFormatHelperService.formatCurrency(result.periodInfo.grandTotal)
            result.periodInfo.fundTotals.keySet().each {
                result.periodInfo.fundTotals[it] = currencyFormatHelperService.formatCurrency(result.periodInfo.fundTotals[it])
            }
        }

        render result as JSON
    }

    private def getLoanText(def loanInfo) {
        def result = [:]
        loanInfo.keySet().each {
            if(loanInfo[it] != 0) {
                result[it] = currencyFormatHelperService.formatCurrency(loanInfo[it])
            }
        }

        if(result.size() > 1) { // need at least 1 loan and procDate for loans to be viewable

            if (loanInfo.procDate) {
                // DateFormat from API = MM/DD/YYYY
                // Convert Date String To Date Object, Json Marshaller will convert Date based on Locale
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                result.procDate = df.parse(loanInfo.procDate);
            }
            return result
        }
        else {
            return null
        }
    }


    /**
     * Gets Financial Aid Award History model for the student
     *
     */
    def getAwardHistory() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = proxyFinAidService.getAwardHistory(pidm);
        result.awards?.each {
            it.data?.rows?.each {
                if (it.fund_title.equals('AWARD_TOTAL')) {
                    it.accept_amt = currencyFormatHelperService.formatCurrency(it.accept_amt)
                    it.cancel_amt = currencyFormatHelperService.formatCurrency(it.cancel_amt)
                    it.decline_amt = currencyFormatHelperService.formatCurrency(it.decline_amt)
                    it.offer_amt = currencyFormatHelperService.formatCurrency(it.offer_amt)
                } else {
                    it.accept_amt = formatCurrencyDashZeroes(it.accept_amt)
                    it.cancel_amt = formatCurrencyDashZeroes(it.cancel_amt)
                    it.decline_amt = formatCurrencyDashZeroes(it.decline_amt)
                    it.offer_amt = formatCurrencyDashZeroes(it.offer_amt)
                }
                it.paid_amt = currencyFormatHelperService.formatCurrency(it.paid_amt)
                it.total_amt = currencyFormatHelperService.formatCurrency(it.total_amt)
            }

            it?.resources?.each {
                it.actual_amt = currencyFormatHelperService.formatCurrency(it.actual_amt)
                it.est_amt = currencyFormatHelperService.formatCurrency(it.est_amt)
            }
        }

        render result as JSON
    }

    def getAccountSummary() {

        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(generalSsbProxyService.getStudentIdFromToken(params.id)))?.pidm

        def result = proxyStudentService.getAccountSummary(pidm);
        result.accountBalTxt = currencyFormatHelperService.formatCurrency(result.accountBal)
        result.acctTotalTxt = currencyFormatHelperService.formatCurrency(result.acctTotal)

        result.terms?.each {
            it.termBalance = currencyFormatHelperService.formatCurrency(it.termBalance)
            it.termCharge = currencyFormatHelperService.formatCurrency(it.termCharge)
            it.termPay = currencyFormatHelperService.formatCurrency(it.termPay)

            it.ledger.each {
                it.balance = currencyFormatHelperService.formatCurrency(it.balance)
                it.charge = it.charge > 0 ? currencyFormatHelperService.formatCurrency(it.charge) : ''
                it.payment = it.payment > 0 ? currencyFormatHelperService.formatCurrency(it.payment) : ''
            }
        }

        render result as JSON
    }

    private def formatCurrencyDashZeroes(def value) {
        def result = '-'
        if(value != 0) {
            result = currencyFormatHelperService.formatCurrency(value)
        }

        return result
    }

    def getWebTailorConfig() {
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

    def getConfig() {
        try {
            render proxyConfigurationService.getProxyGatewayParamsForPayment() as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getProxyConfig() {
        try {
            def pageDisplayInHistoryConfig = ProxyAccessSystemOptionType.fetchByCodeAndSystemCode('PAGE_DISPLAY_IN_HISTORY', 'PROXY')
            def config = [
                    pageDisplayInHistory: pageDisplayInHistoryConfig.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
            ]


            render config as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getCountyList() {
        def map = ProxyControllerUtility.getFetchListParams(params)

        try {
            render countyService.fetchCountyList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getStateList() {
        def map = ProxyControllerUtility.getFetchListParams(params)

        try {
            render stateService.fetchStateList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getNationList() {
        def map = ProxyControllerUtility.getFetchListParams(params)

        try {
            render nationService.fetchNationList(map.max, map.offset, map.searchString) as JSON
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

    private String checkPageForAccess(def tokenizedId, def page) {
        def id = tokenizedId ? generalSsbProxyService.getStudentIdFromToken(tokenizedId) : PersonUtility.getPerson(session["currentStudentPidm"])?.bannerId
        def students = session["students"]?.students?.active
        def student = students?.find { generalSsbProxyService.getStudentIdFromToken(it.id) == id }

        def result = student?.pages?.find { it.url == page }
        return result
    }
}
