/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonUtility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.context.SecurityContextHolder

import static groovy.test.GroovyAssert.*

@Integration
@Rollback
class GeneralSsbProxyServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbProxyService
    def dataSource
    def conn
    def sessionFactory
    def springSecurityService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testTokenError() {
        def result = generalSsbProxyService.setProxy("QUFBVjNnQUFJQUF")

        assertNotNull result.error
    }


    @Test
    void testStudentList() {
        def gidm = getProxyIdm('mrbunny@gvalleyu.edu')
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select user as test1 from dual", {trow ->
        })
        def result = generalSsbProxyService.getStudentListForProxy(gidm)

        assertNotNull result.students

        result.students?.active?.collect {
            it.id = generalSsbProxyService.getStudentIdFromToken(it.id)
        }

        assertTrue result.students.active.id.contains('HOSP0002')
        assertTrue result.students.active.id.contains('HOS00001')
    }


    @Test
    void testStudentListNoAccess() {
        def result = generalSsbProxyService.getStudentListForProxy(-1)

        assertTrue result?.students?.active.size() == 0
    }


    @Test
    void testSetProxyExpiredActionLink() {
        // expired letter SSS_REGD_USER2	SS_PINRESET2    AAAgFJAAFAAAspFAAB
        // set encodedRowId to that letter's row id encoded as base64
        def gidm = getProxyIdm('sss01@ssb.com')
        String encodedRowId = getExpiredLetterEncodedRowId(gidm)
        def result = generalSsbProxyService.setProxy(encodedRowId)

        assertNull result.gidm
        assertTrue result.login
        assertEquals 'tokenExpired', result.message
        assertFalse result.verify
        assertFalse result.doPin
        assertFalse result.error
    }


    @Test
    void testGetProxyPages() {
        def gidm = getProxyIdm('mrbunny@gvalleyu.edu')
        def result = generalSsbProxyService.getProxyPages(gidm, PersonUtility.getPerson("HOSP0002").pidm )

        def page = result.pages.find { it -> it.url.equals("/ssb/proxy/courseScheduleDetail") }

        assertNotNull page
    }


    @Test
    void testPaymentCenterToken() {
        def token
        def gidm = getProxyIdm('mrbunny@gvalleyu.edu')

        SecurityContextHolder?.context?.authentication?.principal?.gidm = new Integer(gidm)

        token = generalSsbProxyService.getPaymentCenterToken()
        assertNotNull token

        def connection = new Sql(sessionFactory.getCurrentSession().connection())
        def idmOut

        Sql sql = new Sql(connection)
        try {
            sql.call("{$Sql.INTEGER = call gokauth.F_GetProxyIDMFromAuthToken(${token})") { idm -> idmOut = idm }
        } catch (e) {
            log.error("ERROR: Could not generate token for the Payment Service. $e")
            throw e
        }

        assertEquals idmOut.toString(), gidm
    }

    @Test
    void testCheckFinaidAccesProxyPages() {
        //Set current proxy
        def gidm = getProxyIdm('mrbunny@gvalleyu.edu')
        SecurityContextHolder?.context?.authentication?.principal?.gidm = new Integer(gidm)

        //Set student ID
        springSecurityService?.getAuthentication()?.user?.pidm = 50200

        //An application exception will not be thrown if the proxy has access to this page.
        try {
            generalSsbProxyService.checkFinaidAccesProxyPages('/ssb/proxy/courseScheduleDetail')
        }
        catch (ApplicationException ae) {
            fail("The proxy should have access to /ssb/proxy/courseScheduleDetail")
        }

        //An application exception should be thrown as the proxy does not have access to this page
        try {
            generalSsbProxyService.checkFinaidAccesProxyPages('/listAwardLetterDetails/')
            fail("The proxy should not have access to /listAwardLetterDetails/")
        }
        catch (ApplicationException ae) {
            assertApplicationException(ae, "You are not authorized to view this page.")
        }
    }


    /*
     * Could probably use net.hedtech.banner.general.events.ProxyAccessCredentialInformation,
     * but don't want to add that dependency this late in the game
     */
    def getProxyIdm(String email) {
        String gidm = '0'
        String gpbprxySql = "select gpbprxy_proxy_idm as gidm from gpbprxy where gpbprxy_email_address = ?"
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow(gpbprxySql, [email]) {trow ->
            gidm = trow.gidm
        }

        return gidm
    }


    def getExpiredLetterEncodedRowId(String gidm) {
        String rowId
        String gpbeltrSql = """select twbkbssf.f_encode(rowid) as row_id from gpbeltr where GPBELTR_PROXY_IDM = ? 
and GPBELTR_SYST_CODE = 'SSS_REGD_USER2' and gpbeltr_ctyp_code = 'SS_PINRESET2'"""
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow(gpbeltrSql, [gidm]) {trow ->
            rowId = trow.row_id.toString()
        }

        return rowId
    }

}
