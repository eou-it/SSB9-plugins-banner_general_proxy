/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.jobsub


import grails.converters.JSON
import net.hedtech.restfulapi.spock.RestSpecification
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec


class JobsubPendingPrintRestFunctionalTests extends RestSpecification {

    static final String localBase = "http://127.0.0.1:8080/BannerGeneralSsb"
    static final String pluralizedResourceName = "jobsub-pending-print"
    static final String printer = "saas1"


    public static String authHeader() {
        def username = 'PRINTID'
        def password = 'u_pick_it'
        def authString = Base64Codec.encode("$username:$password")
        "Basic ${authString}" as String
    }


    def "Test list pending print single printer"() {
        setup:

        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type']= 'application/json'
            headers['Authorization'] = authHeader()
            body {
              """{  "printer" :  "saas1" }"""
            }
        }

        then:
        200 == response.status
      //  'application/json' == responseHeader('X-hedtech-Media-Type')
        "1" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        println "json response: ${json.size()} ${json}"
        "SARADMS" == json[0].job
        "saas1" == json[0].printer
        "saradms_6256.lis" == json[0].fileName
    }
}
