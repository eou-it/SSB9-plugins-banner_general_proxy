/*******************************************************************************
 Copyright 20120 Ellucian Company L.P. and its affiliates.
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
 * Controller for Global Proxy.
 */
class GlobalProxyController {


    static defaultAction = 'landingPage'

    def landingPage() {

        render view: "globalProxy"
    }

}