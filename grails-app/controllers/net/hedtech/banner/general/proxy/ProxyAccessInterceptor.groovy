/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.proxy
import net.hedtech.banner.security.XssSanitizer

class ProxyAccessInterceptor {

    ProxyAccessInterceptor() {
        match controller: ~/(proxy)/
    }

    boolean before() {
        studentIdCheck()
        session["globalProxyAccess"] =  true
    }

    boolean after() { true }

    void afterView() {

        session["globalProxyAccess"] =  false
        // no-op
    }

    private studentIdCheck() {
        def id = XssSanitizer.sanitize(params.id)
        if (id) {
            def students = session["students"]?.students.active
            def student = students?.find { it.id == id }
            if (!student) {
                log.error('Invalid attempt for Id: ' + id)
                redirect(controller: "error", action: "accessForbidden")
                return false
            }
            return true
        }

        return true
    }
}
