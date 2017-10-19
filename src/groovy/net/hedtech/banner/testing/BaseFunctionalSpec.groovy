/* ****************************************************************************
Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.testing

import net.hedtech.banner.security.FormContext
import net.hedtech.restfulapi.spock.RestSpecification

import grails.util.Holders

import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

abstract class BaseFunctionalSpec extends RestSpecification {

    final String RESTFUL_API_BASE_URL = 'http://localhost:' + System.getProperty('server.port', '8080') + '/' + grails.util.Metadata.current.getApplicationName() + '/api'

    def bannerAuthenticationProvider
    def sessionFactory


    void setup() {
        FormContext.set( ['GUAGMNU'] )
        loginLocally()
    }


    void cleanup() {
        loginLocally()
        FormContext.clear()
        SecurityContextHolder.getContext().setAuthentication( null )
    }


    protected String authHeader(username = "grails_user", password = "u_pick_it") {
        def authString = Base64Codec.encode("$username:$password")
        System.out.print(authString)
        "Basic ${authString}" as String
    }


    protected String readOnlyAuthHeader(username = "grails_user_readonly", password = "u_pick_it") {
        def authString = Base64Codec.encode("$username:$password")
        System.out.print(authString)
        "Basic ${authString}" as String
    }


    protected void loginLocally() {
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.characterEncoding = 'UTF-8'
        GrailsWebRequest webRequest = new GrailsWebRequest(request, new MockHttpServletResponse(), Holders.servletContext)
        request.setAttribute(GrailsApplicationAttributes.WEB_REQUEST, webRequest)
        RequestContextHolder.setRequestAttributes(webRequest)
        def token = new UsernamePasswordAuthenticationToken( "grails_user", "u_pick_it" )
        Authentication auth = getBannerAuthenticationProvider().authenticate( token )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }


    def getBannerAuthenticationProvider() {
        if (!bannerAuthenticationProvider) {
            bannerAuthenticationProvider = Holders.applicationContext.getBean("bannerAuthenticationProvider")
        }
        bannerAuthenticationProvider
    }


    def getSessionFactory() {
        if (!sessionFactory) {
            def ctx = Holders.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
            sessionFactory = ctx.getBean("sessionFactory")
        }
        return sessionFactory
    }

}
