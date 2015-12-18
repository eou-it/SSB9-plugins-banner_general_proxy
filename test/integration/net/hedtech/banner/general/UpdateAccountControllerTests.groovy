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

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new UpdateAccountController()
        super.setUp()
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
        loginSSB 'MYE000001', '111111'
        
        controller.request.contentType = "text/json"
        controller.createAccount()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
    }

    @Test
    void testDeleteAccount() {
        loginSSB 'MYE000001', '111111'
        
        controller.request.contentType = "text/json"
        controller.deleteAccounts()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
    }

    @Test
    void testReturnFailureMessage() {
        ApplicationException e = new ApplicationException(DirectDepositAccount, "@@r1:invalidAccountNumFmt@@")

        def failureMessageModel = controller.returnFailureMessage(e)

        assert(failureMessageModel.failure)
        assertEquals("Invalid bank account number format.", failureMessageModel.message)
    }

}
