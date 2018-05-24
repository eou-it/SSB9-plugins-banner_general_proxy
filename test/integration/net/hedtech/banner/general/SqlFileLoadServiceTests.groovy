/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import groovy.sql.OutParameter
import groovy.sql.Sql


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

        assertTrue 0 <= result.size()
    }

    @Test
    void getSqlTextMapEmptyFilename() {
        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = ''

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile

        assertTrue 0 <= result.size()
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
                        sql1: 'select g$_func\n' +
                                '    from dual;'
                ]
        ]

        assertEquals expected, result
    }

    @Test
    void testLoadCollege() {
        def x, y
        def z
        def inValue = "plsql"
        def v

        File f = File.createTempFile('proxySqlFileTest', null)
        f.append('''test {
sqlText = """
        declare

        x VARCHAR2(10);
        y NUMBER(2);
        z DATE;

        in_value VARCHAR2(10);

        my_cur SYS_REFCURSOR;
        begin

        in_value := ?;

        x := 'A';
        y := 10;

        ? := x;
        ? := y;
        ? := TO_DATE('2003/07/09', 'yyyy/mm/dd');

        ? := in_value;

        -- Handle Ref Cursor
        OPEN my_cur
        FOR
        SELECT 'mhitrik' AS my_column FROM dual;

        ? := my_cur;

        end;
       """
}''')

        String oldFile = grailsApplication.config?.proxySqlLoad?.file
        grailsApplication.config?.proxySqlLoad?.file = f.getAbsolutePath()

        def result = sqlFileLoadService.getSqlTextMap()

        grailsApplication.config?.proxySqlLoad?.file = oldFile
        f.delete()

        def sql = new Sql(sessionFactory.getCurrentSession().connection())
        List params = [inValue, Sql.VARCHAR, Sql.NUMERIC, Sql.DATE, Sql.VARCHAR, sqlFileLoadService.CURSOR_PARAMETER]

        sql.call(result.test.sqlText, params,
                { a, b, c, d, data ->
                    x = a
                    y = b
                    z = c
                    v = d
                    data.eachRow(){xo ->println "row:${xo.my_column}"}
                })

        assertNotNull sql

        assertEquals x, "A"
        assertEquals y, 10, 0.0005

        assertEquals v, "plsql"


        String date = '09-07-2003'
        Date d1 = Date.parse( 'dd-MM-yyyy', date )

        assertEquals d1, z

    }

}
