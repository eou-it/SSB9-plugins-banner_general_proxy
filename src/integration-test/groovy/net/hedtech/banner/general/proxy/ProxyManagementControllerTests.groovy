/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.proxy

import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class ProxyManagementControllerTests extends BaseIntegrationTestCase {
    def controller
    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['SELFSERVICE','GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.proxy.ProxyManagementController")
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
    void testListProxies(){
        mockRequest()
        SSBSetUp('GDP000002', '111111')
        controller.request.contentType = "text/json"
        controller.getProxies()
        def data = controller.response.contentAsString

        assertNotNull data

        def jsonData = JSON.parse( data )

        assertNotNull jsonData
        assertTrue jsonData."proxies".size() > 0

    }


    @Test
    void testGetProxy(){
        mockRequest()
        SSBSetUp('A00017091', '111111')

        ProxyControllerUtility.mapProxyGidms([[gidm: 777]])
        controller.request.contentType = "text/json"
        def params = [alt: 0, cver: 0]

        controller.params.putAll(params)
        controller.getProxy()
        def data = controller.response.contentAsString
        assertNotNull data

        def jsonData = JSON.parse( data )
        //check data
        assertNotNull jsonData
        assertNull jsonData?."proxyProfile"?.gidm
        assertNotNull jsonData?."proxyProfile"?.alt
        assertNotNull jsonData?."proxyProfile"?.cver
        //check UI Controls
        assertNotNull jsonData?."proxyUiRules"?."p_passphrase"."visible"
        assertNotNull jsonData?."proxyUiRules"?."p_reset_pin"."visible"

    }


    @Test
    void testGetDataModelOnRelationshipChange(){
        mockRequest()
        SSBSetUp('GDP000002', '111111')
        controller.request.contentType = "text/json"
        def params = [relationshipCode: 'PARENT']

        controller.params.putAll(params)
        controller.getDataModelOnRelationshipChange()
        def data = controller.response.contentAsString
        assertNotNull data

        def jsonData = JSON.parse( data )
        
        //check data
        assertNotNull jsonData
        assertNotNull jsonData.dates
        assertNotNull jsonData.dates.startDate
        assertNotNull jsonData.dates.stopDate
        assertNotNull jsonData.pages

    }


    @Test
    void testGetRelationshipOptions(){
        mockRequest()
        SSBSetUp('GDP000002', '111111')
        controller.request.contentType = "text/json"

        controller.getRelationshipOptions()
        def data = controller.response.contentAsString
        assertNotNull data

        def jsonData = JSON.parse( data )

        //check data
        assertNotNull jsonData
        assertNotNull jsonData.relationships
        assertTrue jsonData.relationships.size() > 0
        assertNotNull jsonData.relationships[0]
        assertNotNull jsonData.relationships[0].code
        assertNotNull jsonData.relationships[0].description

    }


    //@Test
    void testResetProxyPassword(){
        mockRequest()
        SSBSetUp('A00017091', '111111')
        controller.request.contentType = "text/json"
        def params = [gidm: -99999627]

        controller.params.putAll(params)
        controller.resetProxyPassword()
        def data = controller.response.contentAsString
        assertNotNull data

        def jsonData = JSON.parse( data )

        //check data
        assertNotNull jsonData
        assertNotNull jsonData.resetStatus

    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }
}
