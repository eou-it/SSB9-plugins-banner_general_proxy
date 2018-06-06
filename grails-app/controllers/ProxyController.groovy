/*******************************************************************************
 Copyright 2015-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Controller for General
 */
class ProxyController {

    def generalSsbProxyService

    def dataSource               // injected by Spring
    def sessionFactory           // injected by Spring


    def updateProxyProfile(){

        generalSsbProxyService.updateProxyProfile(params)

        def proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)


        render view: "/proxy/proxypersonalinformation",  model :  [proxyProfile: proxyProfile ]
    }

    def proxypersonalinformation(){
        def proxyProfile
        proxyProfile =  generalSsbProxyService.getPersonalInformation(SecurityContextHolder?.context?.authentication?.principal?.gidm)

        render view: "/proxy/proxypersonalinformation", model :  [proxyProfile: proxyProfile ]
    }

    def grades(){
        render view: "/proxy/grades"
    }

    def holds(){
        render view: "/proxy/holds"
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

}
