/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class SqlFileLoadServiceTests extends BaseIntegrationTestCase {

    def sqlFileLoadService
    def grailsApplication


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
    void getSqlTextMapNullFilename() {
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = null

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile

        assertEquals [:], result
    }

    @Test
    void getSqlTextMapEmptyFilename() {
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = ''

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile

        assertEquals [:], result
    }

    @Test
    void getSqlTextMapBadFile() {
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = '/this/aint/a/file'

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile

        assertEquals [:], result
    }

    @Test
    void getSqlTextMapBadSyntax() {
        File f = File.createTempFile('proxySqlFileTest', null)
        f.append('''testSql{
    sql1 = \'\'\'select g$_func
    from dual;\'\'\'
    sql2 { = 'select x from dual'
    }
}''')
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = f.getAbsolutePath()

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile
        f.delete()

        assertEquals [:], result
    }

    @Test
    void getSqlTextMap () {
        File f = File.createTempFile('proxySqlFileTest', null)
        f.append('''testSql{
    sql1 = \'\'\'select g$_func
    from dual;\'\'\'
}''')
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = f.getAbsolutePath()

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile
        f.delete()

        def expected = [
                testSql: [
                        sql1 : 'select g$_func\n' +
                                '    from dual;'
                ]
        ]

        assertEquals expected, result
    }

}
