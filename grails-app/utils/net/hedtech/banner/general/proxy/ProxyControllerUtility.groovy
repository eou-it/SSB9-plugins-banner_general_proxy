/********************************************************************************
  Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.general.proxy

import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.web.context.request.RequestContextHolder

@Slf4j
class ProxyControllerUtility {

    private static final PROXY_GIDM_CACHE = "PROXY_GIDM_CACHE"
    private static final CLONED_PROXY_CODE_CACHE = "CLONED_PROXY_CODE_CACHE"

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
        log.error(e.toString())

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
            log.error(ex)
            model.message = e.message
            return model
        }
    }

    /**
     * Map gidms to alternative IDs in place (i.e. the object passed in is mutated),
     * returning the object passed in.  Handles collections, arrays, and single objects.
     * @param proxies
     * @return Proxies, having gidms replaced with alternate IDs
     */
    static mapProxyGidms(proxies) {
        if (!proxies) return proxies

        def proxyList = isCollectionOrArray(proxies) ? proxies : [proxies]
        def session = RequestContextHolder.currentRequestAttributes().request.session
        def cache = session.getAttribute(PROXY_GIDM_CACHE)

        if (!cache) {
            cache = [:]
            session.setAttribute(PROXY_GIDM_CACHE, cache)
        }

        proxyList.each {
            def altId = ""+cache.size() //Type of key in cache is String
            cache[altId] = it.gidm

            it.alt = altId
            it.remove("gidm")
        }

        return proxies
    }

    static getProxyGidmMapFromSessionCache(id) {
        def session = RequestContextHolder.currentRequestAttributes().request.session
        def cache = session.getAttribute(PROXY_GIDM_CACHE)
        def altId = ""+id //Type of key in cache is String

        cache?."$altId"
    }

    static clearAllProxyGidmMapsFromSessionCache() {
        def session = RequestContextHolder.currentRequestAttributes().request.session
        session.removeAttribute(PROXY_GIDM_CACHE)
    }

    /**
     * Map cloned proxy codes (which are actually gidms) to alternative IDs in place (i.e. the object passed in is mutated),
     * returning the object passed in.  Handles collections, arrays, and single objects.
     * @param clones
     * @return Cloned proxies, having codes replaced with alternate IDs
     */
    static mapClonedProxyCodes(clones) {
        if (!clones) return clones

        def cloneList = isCollectionOrArray(clones) ? clones : [clones]
        def session = RequestContextHolder.currentRequestAttributes().request.session
        def cache = session.getAttribute(CLONED_PROXY_CODE_CACHE)

        if (!cache) {
            cache = [:]
            session.setAttribute(CLONED_PROXY_CODE_CACHE, cache)
        }

        cloneList.each {
            def altCode = ""+cache.size() //Type of key in cache is String
            cache[altCode] = it.code

            it.code = altCode
        }

        return clones
    }

    static getClonedProxyCodeMapFromSessionCache(id) {
        def session = RequestContextHolder.currentRequestAttributes().request.session
        def cache = session.getAttribute(CLONED_PROXY_CODE_CACHE)
        def altCode = ""+id //Type of key in cache is String

        cache?."$altCode"
    }

    static clearAllClonedProxyCodeMapsFromSessionCache() {
        def session = RequestContextHolder.currentRequestAttributes().request.session
        session.removeAttribute(CLONED_PROXY_CODE_CACHE)
    }

    static boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

}
