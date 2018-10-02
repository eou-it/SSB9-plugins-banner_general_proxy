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
        def pidm = PersonUtility.getPerson("GDP000005").pidm
        def result = proxyFinAidService.getAwardPackage(pidm, '1819')

        assertNotNull result
        assertEquals 0, result.size()
    }
}