/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import net.hedtech.banner.exceptions.ApplicationException
import org.grails.plugins.web.taglib.ValidationTagLib
import org.slf4j.Logger
import org.slf4j.LoggerFactory
class ProxyControllerUtility {

    static def logger = LoggerFactory.getLogger('net.hedtech.banner.general.PersonalInformationControllerUtility')

    public static getFetchListParams(params) {
        def maxItems = params.max as int
        def map = [
                max: maxItems,
                offset: (params.offset as int) * maxItems,  // Convert the page-level offset passed as a param to an item-level offset
                searchString: params.searchString
        ]

        map
    }

    public static  returnFailureMessage(ApplicationException e) {
        def model = [:]

        model.failure = true
        logger.error(e)

        try {
            def extractError = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) })
            model.message = extractError.message + (extractError.errors ? " " + extractError.errors : "")

            if(e.type == 'SQLException'){
                // don't expose the oracle error numbers in SQL exceptions
                model.message = model.message.replaceAll("(ORA)-[0-9]+: ","")
            }

            return model
        }
        catch (Exception ex) {
            logger.error(ex)
            model.message = e.message
            return model
        }
    }

}
