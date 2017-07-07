/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import net.hedtech.banner.general.GeneralSsbConfigService

class GeneralSelfServiceFilters {

    def generalSsbConfigService

    def filters = {
        controlAccessToDirectDeposit(controller:'directDeposit|accountListing|updateAccount', action:'*') {
            before = {
                if(generalSsbConfigService.getParamFromSession(GeneralSsbConfigService.ENABLE_DIRECT_DEPOSIT, 'Y') != 'Y') {
                    redirect(controller: "error", action: "accessForbidden")
                    return false
                }
            }
        }

        controlAccessToPersonalInformation(controller:'personalInformation|personalInformationQA', action:'*') {
            before = {
                if(generalSsbConfigService.getParamFromSession(GeneralSsbConfigService.ENABLE_PERSONAL_INFORMATION, 'Y') != 'Y') {
                    redirect(controller: "error", action: "accessForbidden")
                    return false
                }
            }
        }

        controlAccessToPersonalInformationDetails(controller:'personalInformationDetails', action:'*', actionExclude: 'getPersonalDetails|getBannerId|getUserName|getPiConfig') {
            before = {
                if(generalSsbConfigService.getParamFromSession(GeneralSsbConfigService.ENABLE_PERSONAL_INFORMATION, 'Y') != 'Y') {
                    redirect(controller: "error", action: "accessForbidden")
                    return false
                }
            }
        }
    }
}
