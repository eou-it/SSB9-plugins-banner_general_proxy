/*******************************************************************************
 Copyright 2019-2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.system.ProxyAccessSystemOptionType
import net.hedtech.banner.i18n.MessageHelper
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.security.core.context.SecurityContextHolder

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DateTimeException

/**
 * Controller for Proxy Management
 */
class ProxyManagementController {

    def generalSsbProxyManagementService

    static defaultAction = 'landingPage'

    def landingPage() {

        render view: "proxyManagement"
    }


    /*
     Returns the list of Proxies.
    */
    def getProxies() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        def proxies = generalSsbProxyManagementService.getProxyList(pidm)

        ProxyControllerUtility.clearAllProxyGidmMapsFromSessionCache()
        ProxyControllerUtility.mapProxyGidms(proxies.proxies)

        render proxies as JSON
    }

    /*
     Returns the single instance of the Proxy.
    */
    def getProxy() {
        def proxy
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)

            proxy = generalSsbProxyManagementService.getProxyProfile(gidm, pidm)

            // Replace gidm with alternate ID
            proxy.proxyProfile.remove("gidm")
            proxy.proxyProfile.alt = params.alt
            proxy.proxyProfile.cver = params.cver

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy")
            //converts the Date display for I18N
            proxy?.messages?.messages?.each {
                if (it.code == "PIN_EXPIRATION_DATE" || it.code == "EMAIL_VERIFIED" || it.code == "OPTOUT") {

                    it.value = it.value ? df.parse(it.value) : it.value
                }
            }

            render proxy as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }

    }

    /*
     Delete Proxy.
     */
    def deleteProxy() {
        def proxy = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        proxy.pidm = pidm

        try {
            proxy.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(proxy)

            generalSsbProxyManagementService.deleteProxyProfile(proxy)

            def proxies = generalSsbProxyManagementService.getProxyList(pidm)

            ProxyControllerUtility.clearAllProxyGidmMapsFromSessionCache()
            ProxyControllerUtility.mapProxyGidms(proxies.proxies)
            ProxyControllerUtility.invalidateClonedProxyCodeMapCache() // These mappings are also now invalid

            render proxies as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /*
      Create Full Proxy Profile with the Authorization Pages .
    */
    def createUpdateProxy(){

        def map = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        map.pidm = pidm

        try {
            if (!(map?.p_retp_code && isAtLeastOnePageAuthorized(map?.pages))) {
                throw new ApplicationException("", MessageHelper.message("proxyManagement.onSave.REQUIRED"))
            }

            map.code = map.p_code
            map.p_code = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache(map)

            map.gidm = generalSsbProxyManagementService.createProxyProfile(map)

            generalSsbProxyManagementService.updateProxyProfile(map)
            map?.pages?.each{
                def authParams = [:]
                authParams."gidm" = map.gidm
                authParams."pidm" = pidm
                authParams."page" = it.url
                authParams."checked" = it.auth ? "TRUE":"FALSE"
                generalSsbProxyManagementService.manageProxyPagesAuthorization(authParams)
            }

            Map response = [failure: false]
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
     * Get proxy start/stop dates
     */
    def getDataModelOnRelationshipChange() {
        def params = request?.JSON ?: params
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            params.pidm = pidm

            if (params.alt) {
                params.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)
            }

            def startStopDates = generalSsbProxyManagementService.getDataModelOnRelationshipChange(params)

            render startStopDates as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /*
     Updates Proxy.
    */
    def updateProxy(){

        def map = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        map.pidm = pidm

        try {
            map.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(map)

            generalSsbProxyManagementService.updateProxyProfile(map)
            map?.pages?.each{
                def authParams = [:]
                authParams."gidm" = map.gidm
                authParams."pidm" = pidm
                authParams."page" = it.url
                authParams."checked" = it.auth ? "TRUE":"FALSE"
                generalSsbProxyManagementService.manageProxyPagesAuthorization(authParams)
            }

            Map response = [failure: false]
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


    /**
     * Get list of options for Relationship select input control.
     */
    def getRelationshipOptions() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def result = generalSsbProxyManagementService.getRelationshipOptions(pidm)

            render result as JSON
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


    def resetProxyPassword() {

        def params = request?.JSON ?: params
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)
            def status = generalSsbProxyManagementService.resetProxyPassword(gidm, pidm)

            def response = [resetStatus: status, failure: false]
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
     Returns the list of Cloned Proxies.
    */
    def getClonedProxiesList() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        try {
            params.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)

            def proxies = generalSsbProxyManagementService.getProxyClonedList(params)
            ProxyControllerUtility.clearAllClonedProxyCodeMapsFromSessionCache()
            ProxyControllerUtility.mapClonedProxyCodes(proxies.cloneList)

            render proxies as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
    }


    /*
     Returns the list of Cloned Proxies on Create Action.
    */
    def getClonedProxiesListOnCreate() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        try {
            def proxies = generalSsbProxyManagementService.getProxyClonedListOnCreate(params)
            ProxyControllerUtility.clearAllClonedProxyCodeMapsFromSessionCache()
            ProxyControllerUtility.mapClonedProxyCodes(proxies.cloneList)

            render proxies as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
    }


    def getDataModelOnAuthorizationChange() {
        def params = request?.JSON ?: params
        try {
            def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
            params.pidm = pidm
            params.gidm = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache(params)

            def authorizations = generalSsbProxyManagementService.getProxyPages(params)

            render authorizations as JSON

        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    /*
     Returns the list of Add Proxies.
    */
    def getClonedProxyAddList() {

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        def proxies = generalSsbProxyManagementService.getProxyAddList(params)

        ProxyControllerUtility.clearAllProxyAddListPidmMapsFromSessionCache()
        ProxyControllerUtility.mapProxyAddListPidms(proxies.addList)

        render proxies as JSON
    }


    def getCommunicationLog() {
        def params = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        try {
            params.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)

            def communications = generalSsbProxyManagementService.getProxyCommunications(params).communicationsList

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy")

            communications?.each{
                it.actionDate = it.actionDate ? df.parse(it.actionDate) : it.actionDate
                it.expirationDate = it.expirationDate ? df.parse(it.expirationDate) : it.expirationDate
                it.transmitDate = getFormattedTransmitDate(it.transmitDate) //A transmit date needs to be formatted differently because of its timestamp.
            }

            render communications as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
    }


    def getHistoryLog() {
        def params = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        try {
            params.gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)

            def historyLog = generalSsbProxyManagementService.getProxyHistoryLog(params)

            // Internationalize "action" value
            historyLog?.result?.collect {
                it.action = MessageHelper.getMessage("proxyManagement.label.${it.action}")
                it.activityDate = getFormattedTransmitDate(it.activityDate)
            }

            // "length" property needed for xe-table-grid frontend component
            historyLog.length = historyLog.result.size();

            render historyLog as JSON
        } catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage( e ) as JSON
        }
    }


    def getFormattedTransmitDate (String transmitDate) {
        if (userIsInArabicLocale()) {
            def formattedTransmitDate
            try {
                formattedTransmitDate = HijrahCalendarUtils.getHijrahDateWithTimestampFromString(transmitDate)
            }
            catch (DateTimeException e) {
                //A date that does not match the required pattern was provided.
                println(e.message)
                return transmitDate
            }
            return formattedTransmitDate
            }
        else {
            SimpleDateFormat transDateFormat =
                    new SimpleDateFormat(getDateFormat(),  LocaleContextHolder.getLocale())
            DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy HH:mm")
            return transDateFormat.format(transmitDate ? df1.parse(transmitDate) : transmitDate)
        }
    }

    def userIsInArabicLocale () {
        return LocaleContextHolder.getLocale().toString().startsWith('ar')
    }

    def sendCommunicationLog() {
        def data = [:]

        def params = request?.JSON ?: params

        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        params.pidm = pidm

        try {

            def status = generalSsbProxyManagementService.resendProxyCommunicationLog(params)

            def response = [resendStatus: status, failure: false]
            render response as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
        catch (Exception e) {
            log.error(e.toString())
            def response = [message: e.message, failure: true]
            render response as JSON
        }
    }

    def getProxyConfig() {
        def model = [:]

        List<ProxyAccessSystemOptionType> proxyAccessSystemOptionTypes = ProxyAccessSystemOptionType.fetchBySystemCode("PROXY");
        try {
            proxyAccessSystemOptionTypes.each { proxyAccessSystemOptionType ->
                switch (proxyAccessSystemOptionType.code) {
                    case 'ENABLE_DELETE_RELATIONSHIP': model.enableDeleteRelationship = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'ENABLE_DELETE_AFTER_DAYS': model.enableDeleteAfterDays = proxyAccessSystemOptionType.proxyOptdefault.toInteger()
                        break;
                    case 'ENABLE_PASSPHRASE': model.enablePassphrase = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'ENABLE_RESET_PIN': model.enableResetPin = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'ENABLE_TAB_COMMUNICATION': model.enableTabCommunication = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'AUTHORIZATION_IN_HISTORY': model.viewAuthorizationInHistory = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'PAGE_DISPLAY_IN_HISTORY': model.viewPageDisplayInHistory = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'ENABLE_TAB_HISTORY': model.enableTabHistory = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'PAGE_LEVEL_AUTHORIZATION': model.enablePageLevelAuthorization = proxyAccessSystemOptionType.proxyOptdefault.equalsIgnoreCase('Y') ? true : false
                        break;
                    case 'PROXY_GIDM_PREFIX': model.proxyGidmPrefix = proxyAccessSystemOptionType.proxyOptdefault
                        break;
                    default: break;
                }
            }

            render model as JSON
        }
        catch (ApplicationException e) {
            render ProxyControllerUtility.returnFailureMessage(e) as JSON
        }
    }


    def emailAuthentications() {

        def params = request?.JSON ?: params
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)
            def status = generalSsbProxyManagementService.emailAuthentications(gidm, pidm)

            def response = [resetStatus: status, failure: false]
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


    def emailPassphrase() {

        def params = request?.JSON ?: params
        def pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm

        try {
            def gidm = ProxyControllerUtility.getProxyGidmMapFromSessionCache(params)
            def status = generalSsbProxyManagementService.emailPassphrase(gidm, pidm)

            def response = [resetStatus: status, failure: false]
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

    private isAtLeastOnePageAuthorized(pages) {
        if (!pages) {
            return false;
        }

        def found = pages.find {it?.auth}

        found ? true : false
    }

    def static getDateFormat() {
        message("default.dateshorttime.format", null, LocaleContextHolder.getLocale())
    }

    private static String message(key, args = null, locale = null) {
        // copied from banner-general:net.hedtech.banner.MessageUtility rather than introducing new plugin-plugin dependency

        String value = "";
        if (key) {
            if (!locale) locale = Locale.getDefault()
            MessageSource messageSource = Holders.grailsApplication.mainContext.getBean("messageSource")
            value = messageSource.getMessage(key, args, locale)
        }
        return value
    }
}
