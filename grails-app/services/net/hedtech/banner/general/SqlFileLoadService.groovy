/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import groovy.sql.OutParameter
import org.apache.log4j.Logger
import org.codehaus.groovy.control.CompilationFailedException

class SqlFileLoadService {

    private static final log = Logger.getLogger(SqlFileLoadService.class)
    public static final OutParameter CURSOR_PARAMETER = (new OutParameter(){
        public int getType() {
            //return OracleTypes.CURSOR;
            return -10;
        }
    })

    def grailsApplication

    Map getSqlTextMap() {
        String fileLocation
        if (!grailsApplication.config?.proxySqlLoad?.file) {
            log.debug('no config, using resource: /resources/proxySqlLoad.txt')
            fileLocation = getClass().getResource('/resources/proxySqlLoad.txt').getFile()
        }
        else {
            fileLocation = grailsApplication.config?.proxySqlLoad?.file
            log.debug('config found: '+fileLocation)
        }

        try {
            File file = new File(fileLocation)
            String sqlFileText = file.getText('UTF-8')
            def sqlTextMap = new ConfigSlurper().parse(sqlFileText)

            log.debug('proxy sql text map: ' + sqlTextMap)
            return sqlTextMap
        }
        catch (IOException e) {
            log.error('Problem loading SQL file for proxy')
            log.error(e)

            return [:]
        }
        catch(CompilationFailedException e) {
            log.error('Problem building text map from SQL file for proxy')
            log.error(e)

            return [:]
        }
    }
}
