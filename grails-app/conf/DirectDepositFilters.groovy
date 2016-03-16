import net.hedtech.banner.general.DirectDepositUtility
import org.apache.log4j.Logger

class DirectDepositFilters {
    private final log = Logger.getLogger(DirectDepositFilters.class)

    def filters = {
        // Sanitize all parameter values for all actions in the UpdateAccount controller.
        sanitizeFilter(controller:'updateAccount', action:'*') {
            before = {
                def requestParams = request?.JSON ?: params

                if (requestParams in List) {
                    requestParams.each {item -> DirectDepositUtility.sanitizeMap(item)}
                } else if (requestParams in Map) {
                    DirectDepositUtility.sanitizeMap(requestParams)
                } else {
                    log.error(new Exception('Unknown request parameter type. Expected Map or Array.'))
                    return false
                }
            }
        }
    }
}