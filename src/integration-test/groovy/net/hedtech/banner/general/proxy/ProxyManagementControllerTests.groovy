/*******************************************************************************
 Copyright 2019-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.proxy

import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder

import java.text.ParseException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Integration
@Rollback
class ProxyManagementControllerTests extends BaseIntegrationTestCase {
    def controller
    def sessionFactory
    def messageSource

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


    @Test
    void testGetHistoryLogDateFormatsCorrectlyInFrenchLocale() {
        mockRequest()
        SSBSetUp('HOSP0002', '111111')

        final String G_IDM = '-99999985'
        final String P_IDM = '50200'
        final Locale FRENCH_LOCALE = new Locale("fr")
        final Locale PORTUGUESE_BRAZIL_LOCALE = new Locale('pt-BR')
        Locale originalLocale = LocaleContextHolder.getLocale()

        controller.request.contentType = "text/json"
        def params = [alt: 0, cver: 0]
        controller.params.putAll(params)

        ProxyControllerUtility.clearAllProxyGidmMapsFromSessionCache()
        ProxyControllerUtility.mapProxyGidms([[gidm: G_IDM]])

        try {
            createProxyHistory(P_IDM, G_IDM, 'HISTORY');

            //Set locale to French
            LocaleContextHolder.setLocale(FRENCH_LOCALE)

            controller.getHistoryLog()
            def data = controller.response.contentAsString
            assertNotNull data
            def jsonData = JSON.parse(data)

            //Ensure we are correctly testing if dates are the correct format by making sure it doesn't match a different Locale
            assertFalse isDateCorrectFormatForLocale(jsonData.result[0].activityDate, PORTUGUESE_BRAZIL_LOCALE)

            //Now check that this is correctly a French format
            assertTrue isDateCorrectFormatForLocale(jsonData.result[0].activityDate, FRENCH_LOCALE)

        }
        catch (Exception e) {
            fail("Could not successfully retrieve proxy history log: " + e.printStackTrace())
        }
        finally {
            deleteProxyHistory(P_IDM, G_IDM)
            LocaleContextHolder.setLocale(originalLocale)
        }
    }

    @Test
    void testGetHistoryLogDateFormatsCorrectlyInSpanishLocale() {
        mockRequest()
        SSBSetUp('HOSP0002', '111111')

        final String G_IDM = '-99999985'
        final String P_IDM = '50200'
        final Locale SPANISH_LOCALE = new Locale('es')
        final Locale PORTUGUESE_BRAZIL_LOCALE = new Locale('pt-BR')
        Locale originalLocale = LocaleContextHolder.getLocale()

        controller.request.contentType = "text/json"
        def params = [alt: 0, cver: 0]
        controller.params.putAll(params)

        ProxyControllerUtility.clearAllProxyGidmMapsFromSessionCache()
        ProxyControllerUtility.mapProxyGidms([[gidm: G_IDM]])

        try {
            createProxyHistory(P_IDM, G_IDM, 'HISTORY');

            //Set locale to Spanish
            LocaleContextHolder.setLocale(SPANISH_LOCALE)

            controller.getHistoryLog()
            def data = controller.response.contentAsString
            assertNotNull data
            def jsonData = JSON.parse(data)

            //Ensure we are correctly testing if dates are the correct format by making sure it doesn't match a different Locale
            assertFalse isDateCorrectFormatForLocale(jsonData.result[0].activityDate, PORTUGUESE_BRAZIL_LOCALE)

            //Now check that this is correctly an Spanish format
            assertTrue isDateCorrectFormatForLocale(jsonData.result[0].activityDate, SPANISH_LOCALE)

        }
        catch (Exception e) {
            fail("Could not successfully retrieve proxy history log: " + e.printStackTrace())
        }
        finally {
            deleteProxyHistory(P_IDM, G_IDM)
            LocaleContextHolder.setLocale(originalLocale)
        }
    }

    private boolean isDateCorrectFormatForLocale(String date, Locale locale) {
        String dateFormat = messageSource.getMessage("default.dateshorttime.format", null, locale)
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, locale)
        try {
            //If this date is parsable with the locale's format, then it was formatted correctly in the controller.
            LocalDateTime parsedDate = LocalDateTime.parse(date, dateTimeFormatter)
            return true
        }
        catch (ParseException e) {
            println("This date is unparsable with the provided locale's format with message: " + e.getMessage())
            return false
        }
        catch (Exception e) {
            println("An exception occurred while checking if the date is in the expected format: " + e.getMessage())
            return false
        }
    }

    private void createProxyHistory(def pidm, def idm, def page) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.executeUpdate("""
               INSERT INTO gprhist (gprhist_person_pidm, gprhist_proxy_idm,gprhist_page_name,gprhist_activity_date,gprhist_old_auth_ind, gprhist_new_auth_ind ) 
               VALUES(?,?,?, sysdate, 'N', 'Y')
          """, [pidm, idm, page])
        sql.commit();
    }

    private void deleteProxyHistory(def pidm, def idm) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.executeUpdate("""
              DELETE FROM gprhist
                 WHERE gprhist_person_pidm = ?
                 AND gprhist_proxy_idm = ?
          """, [pidm, idm])
        sql.commit();
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }
}
