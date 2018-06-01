/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */


import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.core.context.SecurityContextHolder

import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

/**
 * Controller for General
 */
class GeneralController {

    def log = Logger.getLogger( this.getClass() )
    static defaultAction = "landingPage"

    def generalSsbConfigService
    def generalSsbProxyService

    def dataSource               // injected by Spring
    def sessionFactory           // injected by Spring

    def updateProxyProfile(){

        generalSsbProxyService.updateProxyProfile(params)

        def proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)


        render view: "proxypersonalinformation",  model :  [proxyProfile : proxyProfile ]
    }

    def proxypersonalinformation(){
        def proxyProfile
        proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        render view: "proxypersonalinformation", model :  [proxyProfile : proxyProfile ]
    }

    def grades(){
        render view: "grades"
    }

    def proxy(){

        def result = generalSsbProxyService.setProxy(params.p_token)

        if (result.verify) {

            render view: "actionpassword", params: params, model: [token: params.p_token, gidm : result.gidm]

        } else if (result.login || result.error){

            flash.message = result.message
            forward controller: "login", action: "auth", params: params
        }
    }

    def submitActionPassword() {

        def result = generalSsbProxyService.setProxyVerify(params.token, params.p_verify, params.gidm)

        if (result.doPin) {
            render view: "resetpin", model: [gidm : result.gidm]
        } else {
            forward controller: "login", action: "auth", params: params
        }
    }

    def resetPinAction() {
        println "GUIDM: " + params."gidm" + " " + params.p_pin1 + " " + params.p_pin2 + " " + params.p_email + " " + params.p_pin_orig

        def result = generalSsbProxyService.savePin(params."gidm", params.p_pin1, params.p_pin2, params.p_email, params.p_pin_orig)

        if(!result.errorStatus) {
            forward controller: "login", action: "auth", params: params
        }else{
            flash.message = result.error
            render view: "resetpin"
        }
    }


    def landingPage() {
        try {
            render view: "general"
        } catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }
    }

    def getRoles() {
        def model = [:]
        model.isStudent = hasUserRole( "STUDENT" )
        model.isEmployee = hasUserRole( "EMPLOYEE" )
        model.isAipAdmin = hasUserRole( "ACTIONITEMADMIN" )

        render model as JSON
    }

    /**
     * Get General Configuration
     * @return
     */
    def getGeneralConfig() {
        try {
            def model = generalSsbConfigService.getGeneralConfig()
            render model as JSON
        }
        catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }
    }


    def returnFailureMessage( ApplicationException e ) {
        def model = [:]
        model.failure = true
        log.error( e )
        try {
            model.message = e.returnMap( {mapToLocalize -> new ValidationTagLib().message( mapToLocalize )} ).message
            return model
        } catch (ApplicationException ex) {
            log.error( ex )
            model.message = e.message
            return model
        }
    }

    def hasUserRole( String role ) {
        try {
            def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities
            return authorities.any {it.getAssignedSelfServiceRole().contains( role )}
        } catch (MissingPropertyException it) {
            log.error( "principal lacks authorities - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}" )
            log.error( it )
            throw new ApplicationException( 'DirectDepositAccountCompositeService', it )
        }
    }


    def denied403() {
        render( status: 403 )
    }


}
