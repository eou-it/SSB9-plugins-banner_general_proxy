/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class GeneralSsbConfigServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbConfigService

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
    void testGetParamFromSessionWithNoPreexistingConfig() {
        def val = generalSsbConfigService.getParamFromSession(generalSsbConfigService.ENABLE_DIRECT_DEPOSIT, 'dummy_default_value')

        assertEquals 'Y', val
    }

    @Test
    void testGetParamFromSessionWithPreexistingConfig() {
        def personConfigInSession = [(generalSsbConfigService.getCacheName()): [(generalSsbConfigService.ENABLE_DIRECT_DEPOSIT): 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = generalSsbConfigService.getParamFromSession(generalSsbConfigService.ENABLE_DIRECT_DEPOSIT, 'dummy_default_value')

        assertEquals 'Y', val
    }
}