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

    @Test
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

}
