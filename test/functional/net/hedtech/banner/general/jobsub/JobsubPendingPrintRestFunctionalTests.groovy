/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.jobsub


import grails.converters.JSON
import grails.util.Holders
import net.hedtech.restfulapi.spock.RestSpecification
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.test.annotation.Rollback
import org.junit.After
import org.junit.Before
import org.junit.Test


import static org.codehaus.groovy.grails.plugins.GrailsPluginUtils.*


class JobsubPendingPrintRestFunctionalTests extends RestSpecification {

    static final String localBase = "http://127.0.0.1:8080/BannerGeneralSsb"
    static final String pluralizedResourceName = "jobsub-pending-print"
    static final String badPluralizedResourceName = "jobsubpendingprint"
    static final String printer = "saas1"
    def jobsubSavedOutputService
    def dataSource


    public static String authHeader() {
        def username = 'PRINTID'
        def password = 'u_pick_it'
        def authString = Base64Codec.encode("$username:$password")
        "Basic ${authString}" as String
    }

    public static String badAuthHeader() {
        def username = 'GRAILS'
        def password = 'u_pick_it'
        def authString = Base64Codec.encode("$username:$password")
        "Basic ${authString}" as String
    }




    def "Test list bad Auth"() {
        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("saas1")
        printers.size() == 1
        printers[0].printer == "saas1"
        printers[0].job == "SARADMS"
        printers[0].fileName == "saradms_6256.lis"

        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = badAuthHeader()
            body {
                """{  "printer" :  "saas1" }"""
            }
        }

        then:
        401 == response.status
    }

    def "Test bad resource"() {

        when:
        post("$localBase/qapi/$badPluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "printer" :  "saas1" }"""
            }
        }

        then:
        403 == response.status
    }


    def "Test list pending print single printer"() {
        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("saas1")
        printers.size() == 1
        printers[0].printer == "saas1"
        printers[0].job == "SARADMS"
        printers[0].fileName == "saradms_6256.lis"

        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "printer" :  "saas1" }"""
            }
        }

        then:
        200 == response.status
        "1" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        "SARADMS" == json[0].job
        "saas1" == json[0].printer
        "saradms_6256.lis" == json[0].fileName
    }


    def "Test list pending print no printer"() {

        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("")
        assert printers.size() == 2
        def ind = printers.findIndexOf { it.fileName == "saradms_6256.lis" }
        printers[ind].printer == "saas1"
        printers[ind].job == "SARADMS"
        printers[ind].fileName == "saradms_6256.lis"

        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "printer" :  "" }"""
            }
        }

        then:
        200 == response.status
        //  'application/json' == responseHeader('X-hedtech-Media-Type')
        "2" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        1 == json.findAll { it.job == "SARADMS" && it.printer == "saas1" }.size()
    }


    def "Test list pending print list of printers"() {

        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter(["saas1", "saas2"])
        assert printers.size() == 2
        def ind = printers.findIndexOf { it.fileName == "saradms_6256.lis" }
        printers[ind].printer == "saas1"
        printers[ind].job == "SARADMS"
        printers[ind].fileName == "saradms_6256.lis"

        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "printer" :  "saas1,saas2" }"""
            }
        }

        then:
        200 == response.status
        //  'application/json' == responseHeader('X-hedtech-Media-Type')
        "2" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        1 == json.findAll { it.job == "SARADMS" && it.printer == "saas1" }.size()
    }


    def "Test list pending print invalid printer"() {

        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter(["notsaas"])
        assert printers.size() == 0


        when:
        post("$localBase/qapi/$pluralizedResourceName/") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "printer" :  "notsaas" }"""
            }
        }

        then:
        200 == response.status
        //  'application/json' == responseHeader('X-hedtech-Media-Type')
        "0" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        0 == json.size()
    }


    def "Test list pending print single printer with get"() {
        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("saas1")
        printers.size() == 1
        printers[0].printer == "saas1"
        printers[0].job == "SARADMS"
        printers[0].fileName == "saradms_6256.lis"

        when:
        get("$localBase/api/$pluralizedResourceName?printer=saas1") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
        }

        then:
        200 == response.status
        "1" == responseHeader("x-total-count")
        'List of jobsub-pending-print resources' == responseHeader('X-hedtech-message')
        def json = JSON.parse response.text
        "SARADMS" == json[0].job
        "saas1" == json[0].printer
        "saradms_6256.lis" == json[0].fileName
    }

    def "Test get pending print file"() {
        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("saas1")
        printers.size() == 1
        printers[0].printer == "saas1"
        printers[0].job == "SARADMS"
        printers[0].fileName == "saradms_6256.lis"
        def id =  printers[0].id
        def filename = printers[0].fileName

        when:
        get("$localBase/api/$pluralizedResourceName/${id}") {
            headers['Accept'] = 'application/octet-stream'
            headers['Content-Type'] = 'application/octet-stream'
            headers['Content-Disposition'] = 'Inline;Filename="${fileName}"'
            headers['Authorization'] = authHeader()
        }

        then:
        200 == response.status
        "application/octet-stream" == responseHeader("x-media-type")
        'Details for the jobsub-pending-print resource' == responseHeader('X-hedtech-message')

    }

    // keep test at end, it is destructive
    def "Test put update date"() {

        given:
        def printers = JobsubExternalPrinter.fetchPendingPrintByPrinter("saas1")
        assert printers.size() == 1
        printers[0].printer == "saas1"
        printers[0].job == "SARADMS"
        printers[0].fileName == "saradms_6256.lis"
        def id = printers[0].id


        when:
        put("$localBase/api/$pluralizedResourceName/${id}") {
            headers['Accept'] = 'application/json'
            headers['Content-Type'] = 'application/json'
            headers['Authorization'] = authHeader()
            body {
                """{  "job" :  "SARADMS" }"""
            }
        }

        then:
        200 == response.status


    }
}
