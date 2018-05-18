/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeneralSsbProxyServiceIntegrationTests extends BaseIntegrationTestCase {

    def generalSsbProxyService

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
    void testTokenError() {
        def result = generalSsbProxyService.setProxy("QUFBVjNnQUFJQUF")

        assertNotNull result.error
    }

}