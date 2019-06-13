/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.security.XssSanitizer
import org.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Controller for Proxy Management
 */
class ProxyManagementController {

    def generalSsbProxyManagementService

    static defaultAction = 'landingPage'

    def landingPage() {

        render view: "proxyManagement"
    }

    def getProxies() {
        def pidm = PersonUtility.getPerson(XssSanitizer.sanitize(params.id))?.pidm

        def  proxies = generalSsbProxyManagementService.getProxyList(pidm)

        render proxies as JSON
        }
    }

