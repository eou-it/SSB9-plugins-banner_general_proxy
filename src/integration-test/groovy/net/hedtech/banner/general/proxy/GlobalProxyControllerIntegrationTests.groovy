package net.hedtech.banner.general.proxy

import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.sql.Sql
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class GlobalProxyControllerIntegrationTests extends BaseIntegrationTestCase {
    def controller
    def sessionFactory
    def sql

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['SELFSERVICE', 'GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.proxy.GlobalProxyController")
        updateActiveIndicatorForGlobalProxyTargetRule(true)
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        updateActiveIndicatorForGlobalProxyTargetRule(false)
        super.tearDown()
        super.logout()
    }

    @Test
    void testCheckIfGlobalProxyAccessTargetIsValid() {
        final def GLOBAL_PROXY_ID = 'HOSWEBPL2'
        final def STUDENT_ID = 'HOSR24796'
        final def NON_STUDENT_ID = 'PBDDEV1'
        final def INVALID_ID = 'foo999'
        def returnData

        mockRequest()
        SSBSetUp(GLOBAL_PROXY_ID, '111111')
        controller.request.contentType = "text/json"

        def params = [targetId: STUDENT_ID]
        controller.params.putAll(params)
        controller.checkIfGlobalProxyAccessTargetIsValid()
        assertThatDataExists(controller.response.contentAsString, false)
        returnData = JSON.parse(controller.response.contentAsString)
        assertEquals "true", returnData?.isValidBannerId
        assertEquals "true", returnData?.isValidToBeProxied

        mockRequest()
        params = [targetId: NON_STUDENT_ID]
        controller.params.putAll(params)
        controller.checkIfGlobalProxyAccessTargetIsValid()
        assertThatDataExists(controller.response.contentAsString, false)
        returnData = JSON.parse(controller.response.contentAsString)
        assertEquals "true", returnData?.isValidBannerId
        assertEquals "false", returnData?.isValidToBeProxied

        mockRequest()
        params = [targetId: INVALID_ID]
        controller.params.putAll(params)
        controller.checkIfGlobalProxyAccessTargetIsValid()
        assertThatDataExists(controller.response.contentAsString, false)
        returnData = JSON.parse(controller.response.contentAsString)
        assertEquals "false", returnData?.isValidBannerId
        assertEquals "false", returnData?.isValidToBeProxied

        mockRequest()
        params = [targetId: GLOBAL_PROXY_ID]
        controller.params.putAll(params)
        controller.checkIfGlobalProxyAccessTargetIsValid()
        assertThatDataExists(controller.response.contentAsString, false)
        returnData = JSON.parse(controller.response.contentAsString)
        assertEquals "true", returnData?.isValidBannerId
        //Users cannot target their own ID. This user has the student role,
        //but isValidToBeProxied is false because they are targeting themselves.
        assertEquals "false", returnData?.isValidToBeProxied

    }

    @Test
    void testProxiableUsersPreferredNameIsCorrect() {
        final def GLOBAL_PROXY_ID = 'HOSWEBPL2'
        final def STUDENT_ID = 'HOSR24796'
        final def STUDENT_NAME_LF = 'Nguyen, Thanh'
        def returnData

        try {
            mockRequest()
            SSBSetUp(GLOBAL_PROXY_ID, '111111')
            controller.request.contentType = "text/json"

            //Set to Last, First
            updateProxyAccessPreferredNameSetting("LF")
            params = [targetId: STUDENT_ID]
            controller.params.putAll(params)
            controller.checkIfGlobalProxyAccessTargetIsValid()
            assertThatDataExists(controller.response.contentAsString, false)
            returnData = JSON.parse(controller.response.contentAsString)
            assertEquals STUDENT_NAME_LF, returnData?.preferredName
        }
        finally {
            //Set preferred name setting back to seed data default
            updateProxyAccessPreferredNameSetting("(null)")
        }
    }

    @Test
    void testGetLoggedInUserHasActivePreferredEmail() {
        final def GLOBAL_PROXY_ID = 'HOPTE0606'
        final def GLOBAL_PROXY_PIDM = 30204
        def returnData

        mockRequest()
        SSBSetUp(GLOBAL_PROXY_ID, '111111')
        controller.request.contentType = "text/json"

        try {
            //User has preferred and active email address
            updateActivePreferredEmailAddress(true, true, GLOBAL_PROXY_PIDM)
            controller.getLoggedInUserHasActivePreferredEmail()
            assertThatDataExists(controller.response.contentAsString, false)
            returnData = JSON.parse(controller.response.contentAsString)
            assertTrue returnData?.doesUserHaveActivePreferredEmailAddress

            //User has active email which is not preferred
            mockRequest()
            updateActivePreferredEmailAddress(true, false, GLOBAL_PROXY_PIDM)
            controller.getLoggedInUserHasActivePreferredEmail()
            assertThatDataExists(controller.response.contentAsString, false)
            returnData = JSON.parse(controller.response.contentAsString)
            assertFalse returnData?.doesUserHaveActivePreferredEmailAddress

            //User has preferred email which is not active
            mockRequest()
            updateActivePreferredEmailAddress(false, true, GLOBAL_PROXY_PIDM)
            controller.getLoggedInUserHasActivePreferredEmail()
            assertThatDataExists(controller.response.contentAsString, false)
            returnData = JSON.parse(controller.response.contentAsString)
            assertFalse returnData?.doesUserHaveActivePreferredEmailAddress

            //User has email which is neither active nor preferred
            mockRequest()
            updateActivePreferredEmailAddress(false, false, GLOBAL_PROXY_PIDM)
            controller.getLoggedInUserHasActivePreferredEmail()
            assertThatDataExists(controller.response.contentAsString, false)
            returnData = JSON.parse(controller.response.contentAsString)
            assertFalse returnData?.doesUserHaveActivePreferredEmailAddress
        }
        finally {
            //Set email back to default seed data value
            updateActivePreferredEmailAddress(true, true, GLOBAL_PROXY_PIDM)
        }
    }

    @Test
    void testDeleteGlobalProxyTarget(){
        disableEmailNotificationsForIntegrationTests()

        final def GLOBAL_PROXY_ID = 'HOS00001'
        final def EXISTING_RELATIONSHIP_ID = 'FASS20070'
        final def INVALID_TOKEN = 'AAAAAAA'
        final def NULL_TOKEN = ''
        def studentToken
        def existingProxies
        def targetStudentFoundForGlobalProxy
        def response

        mockRequest()
        SSBSetUp(GLOBAL_PROXY_ID, '111111')

        //Make sure that the Global Proxy actually does have the existing relationship
        controller.request.contentType = "text/json"
        controller.getGlobalProxies()
        assertThatDataExists(controller.response.contentAsString, false)
        existingProxies = JSON.parse(controller.response.contentAsString)
        targetStudentFoundForGlobalProxy = getExistingProxyFromExistingProxyList(existingProxies, EXISTING_RELATIONSHIP_ID)
        assertThatDataExists(targetStudentFoundForGlobalProxy, false)
        assertEquals(EXISTING_RELATIONSHIP_ID, targetStudentFoundForGlobalProxy.bannerId)
        studentToken = targetStudentFoundForGlobalProxy.token

        //Test Null Token
        mockRequest()
        def params = [token: NULL_TOKEN]
        controller.params.putAll(params)
        controller.deleteProxy()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertTrue(response.failure)

        //Test Invalid Token
        mockRequest()
        params = [token: INVALID_TOKEN]
        controller.params.putAll(params)
        controller.deleteProxy()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertTrue(response.failure)

        //Delete the existing relationship
        mockRequest()
        params = [token: studentToken]
        controller.params.putAll(params)
        controller.deleteProxy()
        assertThatDataExists(controller.response.contentAsString, false)
        existingProxies = JSON.parse(controller.response.contentAsString)
        targetStudentFoundForGlobalProxy = getExistingProxyFromExistingProxyList(existingProxies, EXISTING_RELATIONSHIP_ID)
        assertNull targetStudentFoundForGlobalProxy
    }

    @Test
    void testCreateGlobalProxyRelationship(){
        final def NON_EXISTENT_RELATIONSHIP_ID = 'HOSR24796'
        final def INVALID_ID = 'AAAAAAAAA'
        final def NON_STUDENT_ID = 'PBDDEV1'
        final def INVALID_RELATIONSHIP = 'AAA'
        final def NULL_ID = ''
        final def GLOBAL_PROXY_ID = 'HOS00001'
        def targetStudentFoundForGlobalProxy = null
        def existingProxies
        def response

        mockRequest()
        SSBSetUp(GLOBAL_PROXY_ID, '111111')
        disableEmailNotificationsForIntegrationTests()

        //Check to make sure that the non-existent relationship is truly non-existent
        controller.request.contentType = "text/json"
        controller.getGlobalProxies()
        assertThatDataExists(controller.response.contentAsString, false)
        existingProxies = JSON.parse(controller.response.contentAsString)
        targetStudentFoundForGlobalProxy = getExistingProxyFromExistingProxyList(existingProxies, NON_EXISTENT_RELATIONSHIP_ID)
        assertNull(targetStudentFoundForGlobalProxy)

        //Test Invalid ID
        mockRequest()
        def params = [targetBannerId: INVALID_ID, retp: 'UNIV_FINAID']
        controller.params.putAll(params)
        controller.createGlobalProxyRelationship()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertEquals( MessageHelper.message('globalProxyManagement.create.NOPERSON'), response.message)

        //Test Null ID
        mockRequest()
        params = [targetBannerId: NULL_ID, retp: 'UNIV_FINAID']
        controller.params.putAll(params)
        controller.createGlobalProxyRelationship()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertTrue(response.failure)

        //Test Non-Proxiable Target
        mockRequest()
        params = [targetBannerId: NON_STUDENT_ID, retp: 'UNIV_FINAID']
        controller.params.putAll(params)
        controller.createGlobalProxyRelationship()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertEquals( MessageHelper.message('globalProxyManagement.create.NOTARGET'), response.message)

        //Test Invalid Relationship
        mockRequest()
        params = [targetBannerId: NON_EXISTENT_RELATIONSHIP_ID, retp: INVALID_RELATIONSHIP]
        controller.params.putAll(params)
        controller.createGlobalProxyRelationship()
        assertThatDataExists(controller.response.contentAsString, false)
        response = JSON.parse(controller.response.contentAsString)
        assertTrue(response.failure)

        //Add the relationship to the Global Proxy
        mockRequest()
        params = [targetBannerId: NON_EXISTENT_RELATIONSHIP_ID, retp: 'UNIV_FINAID']
        controller.params.putAll(params)
        controller.createGlobalProxyRelationship()
        assertThatDataExists(controller.response.contentAsString, false)
        existingProxies = JSON.parse(controller.response.contentAsString)
        assertThatDataExists(existingProxies?.students?.active, true)
        targetStudentFoundForGlobalProxy = getExistingProxyFromExistingProxyList(existingProxies, NON_EXISTENT_RELATIONSHIP_ID)
        assertThatDataExists(targetStudentFoundForGlobalProxy, false)
        assertEquals(NON_EXISTENT_RELATIONSHIP_ID, targetStudentFoundForGlobalProxy.bannerId)
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    private def assertThatDataExists(data, isList) {
        assertNotNull data

        if (isList) {
            assert data?.size() > 0
        }
    }

    private def updateActiveIndicatorForGlobalProxyTargetRule(enable) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def activeInd = enable ? 'Y' : 'N'
        //Sequence number two is delivered in seed data as rule which restricts global proxy targets to Student roles.
        def sqlText = "UPDATE GORRSQL SET GORRSQL_ACTIVE_IND = ?, GORRSQL_VALIDATED_IND = ? WHERE GORRSQL_SQRU_CODE = 'SSB_ROLE_PROXYTARGET' AND GORRSQL_SEQ_NO = 2"
        sql.executeUpdate(sqlText, [activeInd, activeInd])
        sql.commit()
    }

    private def updateActivePreferredEmailAddress(active, preferred, pidm) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        def activeInd = active ? 'A' : 'I'
        def preferredInd = preferred ? 'Y' : 'N'
        sql.executeUpdate("""UPDATE GOREMAL SET GOREMAL_STATUS_IND = ?, GOREMAL_PREFERRED_IND = ? WHERE GOREMAL_PIDM = ?""", [activeInd, preferredInd, pidm])
        sql.commit()
    }

    private def updateProxyAccessPreferredNameSetting(value) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.executeUpdate("""UPDATE GURNHIR SET GURNHIR_USAGE = ? WHERE GURNHIR_PRODUCT = 'Banner Proxy' AND GURNHIR_APPLICATION = 'ProxyAccess'""", [value])
        sql.commit()
    }

    private def disableEmailNotificationsForIntegrationTests(){
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.executeUpdate("""UPDATE GEBCOMM SET GEBCOMM_PER_NOTIFY_IND = 'N', GEBCOMM_ADMIN_NOTIFY_IND = 'N', GEBCOMM_PROXY_NOTIFY_IND = 'N'
        WHERE GEBCOMM_SYST_CODE= 'PROXY_GLOBAL_ACCESS'""")
        sql.commit()
    }

    private def getExistingProxyFromExistingProxyList(existingProxyList, existingProxyBannerId){
        def targetStudentFoundForGlobalProxy = null
        existingProxyList?.students?.active?.each({activeStudent ->
            if (activeStudent?.bannerId == existingProxyBannerId){
                targetStudentFoundForGlobalProxy = activeStudent
            }
        })
        return targetStudentFoundForGlobalProxy
    }
}
