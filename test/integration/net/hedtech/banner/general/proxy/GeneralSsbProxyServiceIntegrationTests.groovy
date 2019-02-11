/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.security.FormContext

class GeneralSsbProxyServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbProxyService
    def dataSource
    def conn

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']

        if (formContext) {
            FormContext.set( formContext )
        }

        if (useTransactions) {
            sessionFactory.currentSession.with {
                connection().rollback()                 // needed to protect from other tests
                clear()                                 // needed to protect from other tests
                disconnect()                            // needed to release the old database connection
                reconnect( dataSource.getConnection() ) // get a new connection that has unlocked the needed roles
            }
            transactionManager.getTransaction().setRollbackOnly()                 // and make sure we don't commit to the database
            sessionFactory?.queryCache?.clear()                                     //clear the query cache when ehcache is being used
        }
        // super.setup() not called to prevent logging in as admin user which does not have necessary permissions
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
        def result = generalSsbProxyService.getStudentListForProxy(gidm)

        assertNotNull result.students
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
