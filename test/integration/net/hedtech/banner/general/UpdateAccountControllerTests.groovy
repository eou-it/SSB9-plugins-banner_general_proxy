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
        controller.request.json = '''{
            pidm:null,
            status:null,
            apIndicator:"I",
            hrIndicator:"A",
            bankAccountNum:"0822051515",
            amount:null,
            percent:100,
            accountType:"C",
            bankRoutingInfo:{
                bankName:"First Fidelity",
                bankRoutingNum:"123478902",
            },
            amountType:"amount",
            priority:"2",
            newPosition:"2"
        }'''

        controller.createAccount()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
    }

    @Test
    void testDeleteAccount() {
        loginSSB 'MYE000001', '111111'
        
        controller.request.contentType = "text/json"
        controller.request.json = '''[{
            class:"net.hedtech.banner.general.overall.DirectDepositAccount",
            accountType:"C",
            addressSequenceNum:1,
            addressTypeCode:"PR",
            amount:null,
            apAchTransactionTypeCode:null,
            apIndicator:"A",
            bankAccountNum:"9876543",
            bankRoutingInfo:{
                class:"net.hedtech.banner.general.crossproduct.BankRoutingInfo",
                bankName:"First National Bank",
                bankRoutingNum:"234798944",
                dataOrigin:null,
                lastModified:"1999-08-17T03:34:22Z",
                lastModifiedBy:"PAYROLL"
            },
            dataOrigin:"Banner",
            documentType:"D",
            hrIndicator:"I",
            iatAddessSequenceNum:null,
            iatAddressTypeCode:null,
            intlAchTransactionIndicator:"N",
            isoCode:null,
            lastModified:"2016-02-26T19:48:41Z",
            lastModifiedBy:"mye000001",
            percent:100,
            pidm:36732,
            priority:3,
            status:"P",
            apDelete:true
        }]'''

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
