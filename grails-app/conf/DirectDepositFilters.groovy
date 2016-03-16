import net.hedtech.banner.general.DirectDepositUtility

class DirectDepositFilters {
    def filters = {
        // Sanitize all parameter values for all requests in the Direct Deposit application.
        sanitizeFilter(controller:'*', action:'*') {
            before = {
                def map = request?.JSON ?: params
                DirectDepositUtility.sanitizeMap(map)
            }
        }
    }
}