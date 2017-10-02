/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
// Place your Spring DSL code here
import net.hedtech.banner.converters.JobsubOutputMarshaller

beans = {

    jobsubOutputMarshaller(JobsubOutputMarshaller) { bean ->
        bean.autowire = 'byName'
        bean.initMethod = 'init'
    }
}
