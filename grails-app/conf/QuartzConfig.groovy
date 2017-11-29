import org.apache.log4j.Logger

quartz {
    autoStartup = grails.util.Holders.getConfig()?.aip?.scheduler?.enabled ?: true
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = true

    props {
        scheduler.skipUpdateCheck = true
        scheduler.instanceName = 'Action Item Quartz Scheduler'
        scheduler.instanceId = 'AIP' // new instance ID for AIP?
//        scheduler.instanceId = 'AUTO' // new instance ID for AIP?

        if (grails.util.Holders.getConfig()?.aip?.scheduler?.idleWaitTime) {
            scheduler.idleWaitTime = grails.util.Holders.getConfig().aip.scheduler.idleWaitTime
        }

        boolean isWebLogic = grails.util.Holders.getConfig()?.aip?.weblogicDeployment == true
        if (isWebLogic) {
            println( "Setting driverDelegateClass to org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate" )
            jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate'
        } else {
            println( "Setting driverDelegateClass to org.quartz.impl.jdbcjobstore.oracle.OracleDelegate" )
            jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.oracle.OracleDelegate'
        }
            jobStore.class = 'net.hedtech.banner.general.scheduler.quartz.BannerDataSourceJobStoreCMT'
//        jobStore.class = 'net.hedtech.banner.aip.post.scheduler.quartz.ActionItemDataSourceJobStoreCMT' // AIP can share?

        jobStore.tablePrefix = 'GCRQRTZ_' // Share tables. AIP has own instance
        jobStore.isClustered = true
        if (grails.util.Holders.getConfig()?.aip?.scheduler?.clusterCheckinInterval) {
            // Default from Quartz: jobStore.clusterCheckinInterval = 15000
            jobStore.clusterCheckinInterval = grails.util.Holders.getConfig().aip.scheduler.clusterCheckinInterval
        }

//        jobStore.dataSource = 'jdbc/bannerDataSource'
        jobStore.useProperties = false

//        threadPool {
//            class = 'org.quartz.simpl.SimpleThreadPool'
//            threadCount = 10
//            threadPriority = 7
//        }

//        plugin {
//            shutdownhook {
//                'class' = 'org.quartz.plugins.management.ShutdownHookPlugin'
//                cleanShutdown = true
//            }
//        }
    }
}