/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */


import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Controller for General
 */
class GeneralController {

    def log = Logger.getLogger( this.getClass() )
    static defaultAction = "landingPage"

    def generalSsbConfigService

    def proxypersonalinformation(){
        render view: "proxypersonalinformation"
    }

    def grades(){
        render view: "grades"
    }

    def proxy(){
        render view: "actionpassword"
    }

    def submitActionPassword() {
        render view: "resetpin"
    }

    def resetPinAction() {
        //redirect(url: "http://localhost:8080/BannerGeneralSsb/login/auth")
        //redirect(controller:"login", action: "auth")
        redirect(url: "http://" + request.getServerName() + ":" + request.getServerPort() + "/BannerGeneralSsb/login/auth")
        render view: "auth"
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
