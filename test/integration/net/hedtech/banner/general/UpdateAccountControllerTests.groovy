package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.DirectDepositAccount
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class UpdateAccountControllerTests extends BaseIntegrationTestCase {
    def selfServiceBannerAuthenticationProvider
    def updateAccountController

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        updateAccountController = new UpdateAccountController()
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
        super.logout()
    }


    @Test
    void testCreateAccount() {
        updateAccountController.request.contentType = "text/json"
        updateAccountController.createAccount()
        def dataForNullCheck = updateAccountController.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
    }

    @Test
    void testReturnFailureMessage() {
        ApplicationException e = new ApplicationException(DirectDepositAccount, "@@r1:invalidAccountNumFmt@@")

        def failureMessageModel = updateAccountController.returnFailureMessage(e)

        assert(failureMessageModel.failure)
        assertEquals("Invalid bank account number format.", failureMessageModel.message)
    }

}
