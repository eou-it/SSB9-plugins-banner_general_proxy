/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class ProxyFinAidServiceIntegrationTests extends BaseIntegrationTestCase {

    def proxyFinAidService
    def dataSource
    def conn

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testFetchAidYearList() {
        def result = proxyFinAidService.fetchAidYearList()

        assertTrue 3 <= result.size()
        assertTrue result.code.contains('1819')
        assertTrue result.description.contains('2018 - 2019 Aid Year')
        assertTrue result.code.contains('1314')
        assertTrue result.description.contains('1314 aid year (2014)')
        assertTrue result.code.contains('1920')
        assertTrue result.description.contains('2019-2020 Award Year')
    }

    @Test
    void testFetchAidYearListOffset() {
        def result = proxyFinAidService.fetchAidYearList(2, 2, '')

        assertTrue result.size() > 0 && result.size() < 3
        assertTrue result.code.contains('1314')
        assertTrue result.description.contains('1314 aid year (2014)')
        assertTrue result.code.contains('1920')
        assertTrue result.description.contains('2019-2020 Award Year')
    }

    @Test
    void testGetAwardPackage() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def result = proxyFinAidService.getAwardPackage(pidm, '1819')

        assertNotNull result
        assertFalse result.hasAwardInfo
    }
}