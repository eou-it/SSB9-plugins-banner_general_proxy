package net.hedtech.banner.general

import grails.converters.JSON
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import net.hedtech.banner.testing.BaseIntegrationTestCase

import net.hedtech.banner.general.AccountListingController

class AccountListingControllerTests extends BaseIntegrationTestCase {
    def selfServiceBannerAuthenticationProvider
//    def accountListingController

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new AccountListingController()
        super.setUp()
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetApAccountsForCurrentUser(){
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'KRJ000001', '123456' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )

        controller.request.contentType = "text/json"
        controller.getApAccountsForCurrentUser()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        println data
        assertNotNull data
    }

}
