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

    asynchronousBannerAuthenticationSpoofer(AsynchronousBannerAuthenticationSpoofer) {
        dataSource = ref('dataSource')
    }

    // Manage the execution state of the post as a whole
    // This object will scan the post records at regular intervals to determine
    // if the post has completed.
    actionItemPostMonitor(ActionItemPostMonitor) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
        asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
        monitorIntervalInSeconds =  Holders.config.aip?.aipPostMonitor?.monitorIntervalInSeconds ?: 10
    }

    actionItemPostWorkProcessingEngine (ActionItemAsynchronousTaskProcessingEngineImpl) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
        jobManager = ref('actionItemPostWorkTaskManagerService')
        asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
        maxThreads = Holders.config.aip?.aipGroupSendItemProcessingEngine?.maxThreads ?: 10
        maxQueueSize = Holders.config.aip?.aipGroupSendItemProcessingEngine?.maxQueueSize ?: 5000
        continuousPolling = Holders.config.aip?.aipGroupSendItemProcessingEngine?.continuousPolling ?: true
        enabled = Holders.config.aip?.aipGroupSendItemProcessingEngine?.enabled ?: true
        pollingInterval = Holders.config.aip?.aipGroupSendItemProcessingEngine?.pollingInterval ?: 2000
        deleteSuccessfullyCompleted = Holders.config.aip?.aipGroupSendItemProcessingEngine?.deleteSuccessfullyCompleted ?: false
    }

    actionItemJobProcessingEngine (ActionItemAsynchronousTaskProcessingEngineImpl) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
        jobManager = ref('actionItemJobTaskManagerService')
        asynchronousBannerAuthenticationSpoofer = ref('asynchronousBannerAuthenticationSpoofer')
        maxThreads = Holders.config.aip?.aipJobProcessingEngine?.maxThreads ?: 10
        maxQueueSize = Holders.config.aip?.aipJobProcessingEngine?.maxQueueSize ?: 5000
        continuousPolling = Holders.config.aip?.aipJobProcessingEngine?.continuousPolling ?: true
        enabled = Holders.config.aip?.aipJobProcessingEngine?.enabled ?: true
        pollingInterval = Holders.config.aip?.aipJobProcessingEngine?.pollingInterval ?: 2000
        deleteSuccessfullyCompleted = Holders.config.aip?.aipJobProcessingEngine?.deleteSuccessfullyCompleted ?: false
    }

}
