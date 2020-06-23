/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class ProxyControllerUtilityTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new ProxyManagementController()
        super.setUp()
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
    void testMapAndGetAndClearProxyGidmMappingsInSessionCache() {
        def proxies = [[gidm: -111,cver:0],[gidm: -222,cver:0],[gidm: -333,cver:0]]
        ProxyControllerUtility.mapProxyGidms(proxies)
        def cachedInfo = ProxyControllerUtility.getProxyGidmMapFromSessionCache([alt:2,cver:0])

        assertNotNull cachedInfo
        assertEquals new Integer(-333), cachedInfo

        ProxyControllerUtility.clearAllProxyGidmMapsFromSessionCache()
        cachedInfo = ProxyControllerUtility.getProxyGidmMapFromSessionCache([alt:2,cver:0])

        assertNull cachedInfo
    }

    @Test
    void testMapProxyGidmsWithArray() {
        def proxyArr = [[gidm: -111,cver:0],[gidm: -222,cver:0],[gidm: -333,cver:0]] as Object[]

        ProxyControllerUtility.mapProxyGidms(proxyArr)

        def cachedInfo = ProxyControllerUtility.getProxyGidmMapFromSessionCache([alt:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-111), cachedInfo

        cachedInfo = ProxyControllerUtility.getProxyGidmMapFromSessionCache([alt:1,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-222), cachedInfo
    }

    @Test
    void testMapProxyGidmWithSingleObject() {
        def proxy = [gidm: -222,cver:0]

        ProxyControllerUtility.mapProxyGidms(proxy)

        def cachedInfo = ProxyControllerUtility.getProxyGidmMapFromSessionCache([alt:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-222), cachedInfo
    }

    @Test
    void testMapAndGetAndClearClonedProxyCodeMappingsInSessionCache() {
        def proxies = [[code: -111,cver:0],[code: -222,cver:0],[code: -333,cver:0]]
        ProxyControllerUtility.mapClonedProxyCodes(proxies)
        def cachedInfo = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache([alt:2,cver:0])

        assertNotNull cachedInfo
        assertEquals new Integer(-333), cachedInfo

        ProxyControllerUtility.clearAllClonedProxyCodeMapsFromSessionCache()
        cachedInfo = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache([alt:2,cver:0])

        assertNull cachedInfo
    }

    @Test
    void testMapClonedProxyCodeWithArray() {
        def proxyArr = [[code: -111,cver:0],[code: -222,cver:0],[code: -333,cver:0]] as Object[]

        ProxyControllerUtility.mapClonedProxyCodes(proxyArr)

        def cachedInfo = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache([alt:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-111), cachedInfo

        cachedInfo = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache([alt:1,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-222), cachedInfo
    }

    @Test
    void testMapClonedProxyCodeWithSingleObject() {
        def proxy = [code: -222,cver:0]

        ProxyControllerUtility.mapClonedProxyCodes(proxy)

        def cachedInfo = ProxyControllerUtility.getClonedProxyCodeMapFromSessionCache([alt:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(-222), cachedInfo
    }

    @Test
    void testMapAndGetAndClearProxyAddListPidmsMappingsInSessionCache() {
        def proxies = [[code: 111,cver:0],[code: 222,cver:0],[code: 333,cver:0]]
        ProxyControllerUtility.mapProxyAddListPidms(proxies)
        def cachedInfo = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache([code:2,cver:0])

        assertNotNull cachedInfo
        assertEquals new Integer(333), cachedInfo

        ProxyControllerUtility.clearAllProxyAddListPidmMapsFromSessionCache()
        cachedInfo = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache([code:2,cver:0])

        assertNull cachedInfo
    }

    @Test
    void testMapProxyAddListPidmsWithArray() {
        def proxyArr = [[code: 111,cver:0],[code: 222,cver:0],[code: 333,cver:0]] as Object[]

        ProxyControllerUtility.mapProxyAddListPidms(proxyArr)

        def cachedInfo = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache([code:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(111), cachedInfo

        cachedInfo = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache([code:1,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(222), cachedInfo
    }

    @Test
    void testMapProxyAddListPidmsWithSingleObject() {
        def proxy = [code: 222,cver:0]

        ProxyControllerUtility.mapProxyAddListPidms(proxy)

        def cachedInfo = ProxyControllerUtility.getProxyAddListPidmMapFromSessionCache([code:0,cver:0])
        assertNotNull cachedInfo
        assertEquals new Integer(222), cachedInfo
    }

}
