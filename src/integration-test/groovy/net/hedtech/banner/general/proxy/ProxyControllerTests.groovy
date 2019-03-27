/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.*
import grails.util.Holders

import grails.util.GrailsWebMockUtil
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest

@Integration
@Rollback
class ProxyControllerTests extends BaseIntegrationTestCase {
  def controller
    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['SELFSERVICE','GUAGMNU']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.proxy.ProxyController")
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
    void testLandingPage(){
        mockRequest()
        SSBSetUp('MYE000001', '111111')
        controller.request.contentType = "text/json"
        controller.landingPage()
        def dataForNullCheck = controller.response.contentAsString
        assertNotNull dataForNullCheck
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }
}
