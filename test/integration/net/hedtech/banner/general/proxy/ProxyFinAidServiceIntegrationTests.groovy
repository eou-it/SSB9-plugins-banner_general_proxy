package net.hedtech.banner.general.proxy

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
        println result

        assertTrue 2 <= result.size()
        assertTrue result.code.contains('1819')
        assertTrue result.description.contains('2018-2019 Award Year')
    }

    @Test
    void testFetchAidYearListOffset() {
        def result = proxyFinAidService.fetchAidYearList(2, 2, '')
        assertTrue 0 >= result.size()
    }

    @Test
    void testGetAwardPackage() {
        def result = proxyFinAidService.getAwardPackage(37461, '1819')
        println result

        assertNotNull result.unassignedSchedule
        assertEquals 'BIOL', result.unassignedSchedule[0].crse_subj_code
    }
}