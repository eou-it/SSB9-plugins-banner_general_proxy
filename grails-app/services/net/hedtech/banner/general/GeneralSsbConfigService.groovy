/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class GeneralSsbConfigService {

    static final String GENERAL_SSB_CONFIG_CACHE_NAME = 'generalSsbConfig'

    static final String ENABLE_DIRECT_DEPOSIT = 'ENABLE.DIRECT.DEPOSIT'
    static final String ENABLE_PERSONAL_INFORMATION = 'ENABLE.PERSONAL.INFORMATION'

    static final String YES = 'Y'
    static final String NO = 'N'

    def getParamFromSession(param, defaultVal) {
        def personalInfoConfig = getPersonalInfoConfigFromSession()

        def paramVal = personalInfoConfig[param]

        if (!paramVal) {
            log.error("No value found for integration configuration setting \"" + param + "\". " +
                    "This should be configured in GORICCR. Using default value of \"" + defaultVal + "\".")

            paramVal = defaultVal
        }

        paramVal
    }

    private static getPersonalInfoConfigFromSession() {
        def personConfigInSession = PersonUtility.getPersonConfigFromSession()

        if (personConfigInSession) {
            if (!personConfigInSession.containsKey(GENERAL_SSB_CONFIG_CACHE_NAME)) {
                createPersonalInfoConfig(personConfigInSession)
            }
        } else {
            createPersonalInfoConfig(personConfigInSession)
            PersonUtility.setPersonConfigInSession(personConfigInSession)
        }

        personConfigInSession[GENERAL_SSB_CONFIG_CACHE_NAME]
    }

    private static createPersonalInfoConfig(personConfigInSession) {
        def configFromGoriccr = IntegrationConfiguration.fetchAllByProcessCode('GENERAL_SSB')
        def config = [:]

        configFromGoriccr.each {it ->
            config[it.settingName] = it.value
        }

        personConfigInSession[GENERAL_SSB_CONFIG_CACHE_NAME] = config

        personConfigInSession
    }
}