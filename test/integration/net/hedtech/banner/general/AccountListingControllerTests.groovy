package net.hedtech.banner.general

import grails.converters.JSON
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

import net.hedtech.banner.testing.BaseIntegrationTestCase

class AccountListingControllerTests extends BaseIntegrationTestCase {
    def selfServiceBannerAuthenticationProvider
    def accountListingController

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        accountListingController = new AccountListingController()
        super.setUp()
        def auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( 'MYE000001', '111111' ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetMyAccounts(){
        accountListingController.request.contentType = "text/json"
        accountListingController.getMyAccounts()
        def dataForNullCheck = accountListingController.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        println data
        assertNotNull data
    }

}
