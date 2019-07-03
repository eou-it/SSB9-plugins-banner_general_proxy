/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class GeneralSsbProxyManagementServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbProxyManagementService
    def dataSource
    def conn
    def sessionFactory

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    //@Test
    void testProxyList() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm

        def result = generalSsbProxyManagementService.getProxyList(pidm)

        assertTrue result.proxies.size > 0
        assertNotNull result.proxies[0]."firstName"
        assertNotNull result.proxies[0]."lastName"
        assertNotNull result.proxies[0]."email"
        assertNotNull result.proxies[0]."gidm"
    }


    //@Test
    void createProxy() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
            assertNotNull gidm

            def result = generalSsbProxyManagementService.getProxyList(pidm)

            assertTrue result.proxies.findAll { it.email == "a@aol.com" }.size() > 0

            assertTrue checkUrl(gidm)

        } catch (Exception e) {
            fail("Could not generate IDM. " + e)
        } finally {
            deleteDBEntry(gidm);
        }
    }

    //@Test
    void verifyEmailProxy() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol1.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
        } catch (Exception e) {
            assertTrue e.getMessage().contains("NOEMAILMATCH")
        } finally {
            deleteDBEntry(gidm);
        }
    }

    //@Test
    void testEmailFormat() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "aaol.com"
        params."p_email_verify" = "aaol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
        } catch (Exception e) {
            assertTrue e.getMessage().contains("BADEMAIL")
        } finally {
            deleteDBEntry(gidm);
        }
    }

    //@Test
    void testRequiredLastNameOrFirstNameOrEmail() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            params."p_email" = ""
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
        } catch (Exception e) {
            assertTrue e.getMessage().contains("REQUIRED")
        } finally {
            deleteDBEntry(gidm);
        }

        try {
            params."p_last" = ""
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
        } catch (Exception e) {
            assertTrue e.getMessage().contains("REQUIRED")
        } finally {
            deleteDBEntry(gidm);
        }

        try {
            params."p_first" = ""
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
        } catch (Exception e) {
            assertTrue e.getMessage().contains("REQUIRED")
        } finally {
            deleteDBEntry(gidm);
        }
    }

    //@Test
    void testEmailInUse() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
            fail("Email In Use")
        } catch (Exception e) {
            assertTrue e.getMessage().contains("EMAILINUSE")
        } finally {
            deleteDBEntry(gidm);
        }
    }


    //@Test
    void testShowProxyProfile() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
            def profile = generalSsbProxyManagementService.getProxyProfile(gidm, pidm)

            assertTrue gidm == profile?.proxyProfile?.gidm
            assertTrue pidm == profile?.proxyProfile?.pidm
            assertEquals "AAA", profile?.proxyProfile?.p_retp_code
            assertNotNull profile?.proxyProfile?.p_start_date
            assertNotNull profile?.proxyProfile?.p_stop_date
            assertNull profile?.proxyProfile?.p_desc

            assertNotNull profile?.proxyUiRules?."p_passphrase"."visible"
            assertNotNull profile?.proxyUiRules?."p_reset_pin"."visible"

        } catch (Exception e) {
            fail("Could get Proxy Profile. " + e)
        } finally {
            deleteDBEntry(gidm);
        }
    }


    @Test
    void testDeleteProxyProfile() {
        def id = 'A00017091'

        def pidm = PersonUtility.getPerson(id)?.pidm
        def gidm
        def params = [:]
        params."p_email" = "a@aol.com"
        params."p_email_verify" = "a@aol.com"
        params."p_last" = "Abc"
        params."p_first" = "ABX"
        params."pidm" = pidm

        try {
            gidm = generalSsbProxyManagementService.createProxyProfile(params)
            def profile = generalSsbProxyManagementService.getProxyProfile(gidm, pidm)

            assertTrue gidm == profile?.proxyProfile?.gidm
            assertTrue pidm == profile?.proxyProfile?.pidm
            assertEquals "AAA", profile?.proxyProfile?.p_retp_code
            assertNotNull profile?.proxyProfile?.p_start_date
            assertNotNull profile?.proxyProfile?.p_stop_date
            assertNull profile?.proxyProfile?.p_desc

            assertNotNull profile?.proxyUiRules?."p_passphrase"."visible"
            assertNotNull profile?.proxyUiRules?."p_reset_pin"."visible"

            params.gidm = gidm

            generalSsbProxyManagementService.deleteProxyProfile(params)

            profile = generalSsbProxyManagementService.getProxyProfile(gidm, pidm)

            assertNull profile?.proxyProfile?.gidm

        } catch (Exception e) {
            fail("Could not Delete Proxy Profile. " + e)
        } finally {
            deleteDBEntry(gidm);
        }
    }


    @Test
    void testGetProxyStartStopDates() {
        def relationshipCode = "PARENT"
        def startStopDates = generalSsbProxyManagementService.getProxyStartStopDates(relationshipCode)

        assertNotNull startStopDates
        assertNotNull startStopDates.startDate
        assertNotNull startStopDates.stopDate
        assertTrue   startStopDates.stopDate.after(startStopDates.startDate)
    }


    @Test
    void testGetRelationshipOptions() {
        def id = 'A00017091'
        def pidm = PersonUtility.getPerson(id)?.pidm

        def result = generalSsbProxyManagementService.getRelationshipOptions(pidm)

        assertNotNull result
        assertNotNull result.relationships
        assertTrue result.relationships.size() > 0
        assertNotNull result.relationships[0]
        assertNotNull result.relationships[0].code
        assertNotNull result.relationships[0].description
    }



    void deleteDBEntry(def idm) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.executeUpdate("""
                delete from GPRXREF
                where GPRXREF_proxy_idm = ?
          """, [idm])
            sql.executeUpdate("""
                delete from gpbprxy
               where gpbprxy_proxy_idm = ?
           """, [idm])
            sql.commit();
        } finally {
            //sql?.close()
        }
    }


    def checkUrl(def idm){
        def url

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            def query = """SELECT gpbeltr_ctyp_url url FROM gpbeltr WHERE gpbeltr_proxy_idm = :idm
                           and gpbeltr_ctyp_code = 'NEW_PROXY_NOA' """
            url = sql?.firstRow(query,[idm: idm])?.url
        } finally {
            //sql?.close()
        }

        url.contains("BannerGeneralSsb/ssb/proxy/proxyAction?p_token")
    }
}
