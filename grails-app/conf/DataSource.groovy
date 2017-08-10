/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

// Support Hibernate annotations
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration


// Note: Most of the dataSource configuration resides in resources.groovy and in the
// installation-specific configuration file (see Config.groovy for the include).

dataSource {
    configClass = GrailsAnnotationConfiguration.class
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    loggingSql = false

}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    //cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'

    cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory' // Hibernate 3
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context


//    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
    hbm2ddl.auto = null
    show_sql = false
    // naming_strategy = "org.hibernate.cfg.ImprovedNamingStrategy"
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    config.location = [
            "classpath:hibernate-banner-core.cfg.xml",
            "classpath:hibernate-banner-aip.cfg.xml",
            "classpath:hibernate-banner-general-person.cfg.xml",
            "classpath:hibernate-banner-general-validation-common.cfg.xml",
            "classpath:hibernate-banner-general-common.cfg.xml",
    ]
}

/*
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
    hbm2ddl.auto = null
    show_sql = false
    // naming_strategy = "org.hibernate.cfg.ImprovedNamingStrategy"
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    config.location = [
            "classpath:hibernate-banner-core.cfg.xml",
            "classpath:hibernate-banner-general-person.cfg.xml",
            "classpath:hibernate-banner-general-validation-common.cfg.xml",
            "classpath:hibernate-banner-general-common.cfg.xml"
    ]
}

*/


// environment specific settings
environments {
    development {

        //Banner database (default)
        dataSource {
        }

        //Database with sspb
        dataSource_sspb {
            /*   ---> moved to banner_configuration.groovy
            */
        }

    }
    test {
        //Banner database (default)
        dataSource {
        }
        //Database with sspb
        dataSource_sspb {
        }
    }
    production {
        dataSource {
        }
        //Database with sspb
        dataSource_sspb {
        }
    }
}