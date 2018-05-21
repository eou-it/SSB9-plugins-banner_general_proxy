/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import org.apache.log4j.Logger
import org.codehaus.groovy.control.CompilationFailedException

class SqlFileLoadService {

    private static final log = Logger.getLogger(SqlFileLoadService.class)
    def grailsApplication

    Map getSqlTextMap() {
        String fileLocation = grailsApplication.config?.proxySqlLoad?.file
        if (!fileLocation) {
            return [:]
        }

        try {
            File file = new File(fileLocation)
            String sqlFileText = file.getText('UTF-8')
            def sqlTextMap = new ConfigSlurper().parse(sqlFileText)

            log.info('proxy sql text map: ' + sqlTextMap)
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
