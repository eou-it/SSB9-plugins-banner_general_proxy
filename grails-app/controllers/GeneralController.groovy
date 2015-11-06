/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */


import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.LocalizeUtil
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import net.hedtech.banner.positioncontrol.utility.UsersUtility

class GeneralController {

    def log = Logger.getLogger( this.getClass() )
    static defaultAction = "landingPage"


    def fetchDate() {
        def map = ['date': new Date(), 'dateFormat': LocalizeUtil.dateFormat]
        render map as JSON
    }


    def landingPage() {
        try {
            //TODO: call fetch roles
            def model = [:]
            def url = g.message( code: 'default.url.landing.page.for.roles' )

            model = ['url': url]
//            render model: model, view: "generalSsb"
            render model: model, view: "general"
        } catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }
    }
    
    def getRoles() {
        def model = [:]
        model.isStudent = UsersUtility.hasUserRole("STUDENT")
        model.isEmployee = UsersUtility.hasUserRole("EMPLOYEE")
        
        render model as JSON
    }


    def  returnFailureMessage(ApplicationException  e) {
        def model = [:]
        model.failure = true
        log.error(e)
        try {
            model.message = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) }).message
            return model
        } catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }


}
