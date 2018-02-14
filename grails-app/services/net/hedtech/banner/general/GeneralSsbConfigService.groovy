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
    static final String ENABLE_ACTION_ITEM = 'ENABLE.ACTION.ITEMS'
    def actionItemProcessingConfigService


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

        [isDirectDepositEnabled      : getParamFromSession( ENABLE_DIRECT_DEPOSIT, 'Y' ) == 'Y',
         isPersonalInformationEnabled: getParamFromSession( ENABLE_PERSONAL_INFORMATION, 'Y' ) == 'Y',
         isActionItemEnabledAndAvailable         : getParamFromSession( ENABLE_ACTION_ITEM, 'Y' ) == 'Y' && actionItemProcessingConfigService.isActionItemPresentForUser(),
         isActionItemEnabled :  getParamFromSession( ENABLE_ACTION_ITEM, 'Y' ) == 'Y']
    }
}
