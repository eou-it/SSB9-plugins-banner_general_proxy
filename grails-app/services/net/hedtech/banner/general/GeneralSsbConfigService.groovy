/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

/**
 * Service Class for General SSB Configuration
 */
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

    /**
     * Get General Configuration
     * @return
     */
    def getGeneralConfig() {
        [isDirectDepositEnabled      : getParamFromSession( GeneralSsbConfigService.ENABLE_DIRECT_DEPOSIT, 'Y' ) == 'Y',
         isPersonalInformationEnabled: getParamFromSession( GeneralSsbConfigService.ENABLE_PERSONAL_INFORMATION, 'Y' ) == 'Y',
         isActionItemEnabled         : getParamFromSession( GeneralSsbConfigService.ENABLE_ACTION_ITEM, 'Y' ) == 'Y']
    }
}
