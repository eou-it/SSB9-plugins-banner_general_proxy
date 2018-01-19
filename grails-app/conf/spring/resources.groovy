/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import grails.util.Holders
import net.hedtech.api.security.RestApiAccessDeniedHandler
import net.hedtech.api.security.RestApiAuthenticationEntryPoint
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.aip.post.engine.ActionItemAsynchronousTaskProcessingEngineImpl
import net.hedtech.banner.aip.post.grouppost.ActionItemPostMonitor
import net.hedtech.banner.PbBannerRestfulServiceAdapter

// Place your Spring DSL code here
import net.hedtech.banner.converters.JobsubOutputMarshaller

beans = {
    //bean being used by rest api, adapter PageBuilder to Banner
    restfulServiceAdapter(PbBannerRestfulServiceAdapter)

    //Banner equivalent of bean above
    //bannerRestfulApiServiceBaseAdapter(RestfulApiServiceBaseAdapter)

    restApiAuthenticationEntryPoint(RestApiAuthenticationEntryPoint) {
        realmName = 'Banner REST API Realm'
    }

    restApiAccessDeniedHandler(RestApiAccessDeniedHandler)


    jobsubOutputMarshaller(JobsubOutputMarshaller) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
    }
}
