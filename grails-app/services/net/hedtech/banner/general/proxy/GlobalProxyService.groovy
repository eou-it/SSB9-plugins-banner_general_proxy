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
