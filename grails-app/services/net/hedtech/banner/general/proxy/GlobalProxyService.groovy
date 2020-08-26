/********************************************************************************
  Copyright 2020 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import grails.gorm.transactions.Transactional

@Transactional
class GlobalProxyService {
    def personEmailService

    def doesUserHaveActivePreferredEmail(pidm) {
        def userPreferredEmail = personEmailService.findPreferredEmailAddress(pidm)
        def doesUserHaveActivePreferredEmail = userPreferredEmail != null
        doesUserHaveActivePreferredEmail
    }
}
