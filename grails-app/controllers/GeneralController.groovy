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

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        //get proxy gidm
        def p_proxyIDM = SecurityContextHolder?.context?.authentication?.principal?.gidm
        def p_first_name = params.p_first_name
        def p_last_name = "Hitrik"


        //Execute bwgkpxya.P_PA_StoreProfile
        sql.executeUpdate("""
       DECLARE
        lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
        lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;
       BEGIN
            -- Get the proxy record
          lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (${p_proxyIDM});
          FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
          CLOSE lv_GPBPRXY_ref;
          
          gp_gpbprxy.P_Update (
            p_proxy_idm    => ${p_proxyIDM},
            p_first_name   => ${p_first_name},
            p_last_name    => ${p_last_name},
            p_user_id      => goksels.f_get_ssb_id_context,
            p_rowid        => lv_GPBPRXY_rec.R_INTERNAL_RECORD_ID
            );
            
            gb_common.P_Commit;
       
        END ;
        
            """)

        println params
        render view: "proxypersonalinformation"
    }

    def proxypersonalinformation(){
        render view: "proxypersonalinformation"
    }

    def grades(){
        render view: "grades"
    }

    def proxy(){

        def result = generalSsbProxyService.setProxy(params.p_token)

        if (result.verify) {

            render view: "actionpassword", params: params

        } else if (result.login || result.error){

            flash.message = result.message
            forward controller: "login", action: "auth", params: params
        }
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
