/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

class GeneralSsbConfigService extends BasePersonConfigService {

    static final String ENABLE_DIRECT_DEPOSIT = 'ENABLE.DIRECT.DEPOSIT'
    static final String ENABLE_PERSONAL_INFORMATION = 'ENABLE.PERSONAL.INFORMATION'

    @Override
    protected String getCacheName() {
        return 'generalSsbConfig'
    }

    @Override
    protected String getProcessCode() {
        return 'GENERAL_SSB'
    }

    @Override
    protected List getExcludedProperties() {
        return []
    }
}