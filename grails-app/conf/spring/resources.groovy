/*******************************************************************************
 Copyright 2013-2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import net.hedtech.api.security.RestApiAccessDeniedHandler
import net.hedtech.api.security.RestApiAuthenticationEntryPoint
import net.hedtech.banner.PbBannerRestfulServiceAdapter

/**
 * Spring bean configuration using Groovy DSL, versus normal Spring XML.
 */
beans = {

    //bean being used by rest api, adapter PageBuilder to Banner
    restfulServiceAdapter(PbBannerRestfulServiceAdapter)

    //Banner equivalent of bean above
    //bannerRestfulApiServiceBaseAdapter(RestfulApiServiceBaseAdapter)

    restApiAuthenticationEntryPoint(RestApiAuthenticationEntryPoint) {
        realmName = 'Banner REST API Realm'
    }

    restApiAccessDeniedHandler(RestApiAccessDeniedHandler)

}
