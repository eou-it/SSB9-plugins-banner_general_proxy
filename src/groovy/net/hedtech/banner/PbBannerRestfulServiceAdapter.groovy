/*******************************************************************************
 * Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/

package net.hedtech.banner

import net.hedtech.banner.restfulapi.RestfulApiServiceBaseAdapter
import net.hedtech.restfulapi.RestfulServiceAdapter

/*
This adapter makes makes it possible to use the bannerRestfulApiSupport plugin with PageBuilder.
This is needed because the PageBuilder services were built to be compatible with the Restful API plugin and require the params.
*/

class PbBannerRestfulServiceAdapter implements RestfulServiceAdapter  {

    def bannerAdapter = new RestfulApiServiceBaseAdapter()

    def list(def service, Map params) {
        bannerAdapter.list(service, params)
    }

    def count(def service, Map params) {
        bannerAdapter.count(service, params)
    }

    def show(def service, Map params) {
        if ((service.metaClass.respondsTo(service, "show", Map))) {
            service.show(params)
        } else {
            bannerAdapter.show(service, params)
        }
    }

    def create(def service, Map content, Map params) {
        if ((service.metaClass.respondsTo(service, "create", Map, Map))) {
            service.create(content, params)
        } else {
            bannerAdapter.create(service, content, params)
        }
    }

    def update(def service, Map content, Map params) {
        if ((service.metaClass.respondsTo(service, "update", Map, Map))) {
            service.update(content,params)
        } else {
            bannerAdapter.update(service, content, params)
        }
    }

    void delete(def service, Map content, Map params) {
        if ((service.metaClass.respondsTo(service, "delete", Map, Map))) {
            service.delete(content,params)
        } else {
            bannerAdapter.delete(service, content, params)
        }
    }

}
