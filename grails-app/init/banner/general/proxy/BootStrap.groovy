package banner.general.proxy

/*******************************************************************************
Copyright 2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import net.hedtech.banner.converters.json.JSONBeanMarshaller
import net.hedtech.banner.converters.json.JSONDomainMarshaller
import net.hedtech.banner.i18n.LocalizeUtil
import grails.converters.JSON
import grails.util.Environment
import grails.core.ApplicationAttributes
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.context.i18n.LocaleContextHolder as LCH
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * */
class BootStrap {
    def init = { servletContext ->
    }
    def destroy = {
    }
}
