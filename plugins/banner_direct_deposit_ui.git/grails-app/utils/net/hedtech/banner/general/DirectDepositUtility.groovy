package net.hedtech.banner.general

import net.hedtech.banner.security.XssSanitizer

class DirectDepositUtility {

    /**
     * Recursively sanitize all values in map to eliminate cross-site scripting (XSS) vulnerabilities.
     * @param map
     */
    def static sanitizeMap(Map map) {
        map.each { element ->
            def v = element.value

            if (v in Map) {
                sanitizeMap(v)
            } else if (v in String) {
                element.value = XssSanitizer.sanitize(v)
            }
        }
    }

}
