/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ResponseParseException


import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.PUT
import grails.converters.JSON

/**
 * Demo of rest commands from groovy
 *
 * curl command for put: curl -X PUT -H "Content-Type: application/json" -H "Accept: application/json"
 -H "Authorization: Basic bWhvY2tldHQ6dV9waWNrX2l0" -d '{"job":"SARADMS"}'
 "http://localhost:8080/BannerGeneralSsb/api/jobsubPendingPrint/8"

 Groovy script to get list of pending print; show the pending print job; put pending print job print date

 script uses grails_user as test,  which needs to have SELFSERVICE object

 Insert Into Guruobj(Guruobj_Object,Guruobj_Role,Guruobj_Userid,Guruobj_Activity_Date,Guruobj_User_Id,Guruobj_Comments,Guruobj_Data_Origin)
 select 'SELFSERIVCE','BAN_DEFAULT_M','GRAILS_USER',sysdate,user,'TEST','BANNER' from dual
 where not exists (select 1 from guruobj
 Where Guruobj_Userid = 'GRAILS_USER'
 And Guruobj_Object = 'SELFSERIVCE');

 */

def userName = "MHOCKETT"
def password = "u_pick_it"
def encodedId = (userName + ":" + password).bytes.encodeBase64().toString()
println "${encodedId}"


target(jobsubRestPrint: "Jobsub Restful Print") {


    def results = []
    def printer = "saas1"

    results = getPendingPrintList(encodedId, printer)

    results?.each { res ->
        println "Before Get file for job ${res.job} filename ${res.fileName} oneUp ${res.oneUpNo}"
        getPrintFile(encodedId, res)

    }
}

setDefaultTarget "jobsubRestPrint"


def getPendingPrintList(def encodedId, def printer) {
    def results = []
    try {

        def pendingPrint = new HTTPBuilder()
        pendingPrint.request("http://localhost:8080/", GET, groovyx.net.http.ContentType.JSON) { req ->
            uri.path = "/BannerGeneralSsb/api/jobsub-pending-print/"
            uri.query = [printer: printer]
            headers.Accept = "application/json"
            headers.ContentType = "application/json"
            headers.Authorization = "Basic ${encodedId}"
            contentType = "application/json"
            requestContentType = ContentType.JSON

            response.success = { resp, json ->

                results = json
                println "Got pending list success response: ${resp.statusLine}"
//                println "Success Content-Type: ${resp.headers.'Content-Type'}"
//                resp.headers.each {
//                    println "success response header each ${it}"
//                }
            }

            response.failure = { resp ->
                println "Got failure response: ${resp.statusLine}"
                println "Failure Content-Type: ${resp.headers.'Content-Type'}"
            }


        }
    } catch (java.net.UnknownHostException t) {
        println "UnknownHostException Error trying to send   ${t}."
    } catch (org.apache.http.conn.HttpHostConnectException t) {
        println "HttpHostConnectException Error trying to send   ${t}."
    } catch (javax.net.ssl.SSLPeerUnverifiedException t) {
        println "SSLPeerUnverifiedException Error trying to send   ${t}"
    } catch (groovyx.net.http.ResponseParseException t) {

        String contentType
        try {
            contentType = t.response?.contentType
            println "content type: ${contentType}"
            println "parse exception: ${t}"
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
        }
        println "ResponseParseException Error trying to send Parse exception. Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
        t.printStackTrace()
    } catch (groovyx.net.http.HttpResponseException t) {

        String contentType
        try {
            println "response exception ${t}"
            contentType = t.response?.contentType
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
            println "exception illegal arg ${e}"
        }
        println "HttpResponseException Error trying to send. Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
    }
    return results

}


def getPrintFile(def encodedId, def jobResults) {
    def results = []
    println "Inside get print file ${jobResults}"
    try {


        def pendingPrint = new HTTPBuilder()
        pendingPrint.request("http://localhost:8080/", GET, groovyx.net.http.ContentType.JSON) { req ->
            uri.path = "/BannerGeneralSsb/api/jobsub-pending-print/${jobResults.id}"
            headers.Accept = "application/json"
            headers.ContentType = "APPLICATION/OCTET-STREAM"
            headers.Authorization = "Basic ${encodedId}"
            headers.'Content-Disposition' = "Attachment;Filename=${jobResults.fileName}"

            response.success = { resp, json ->
                results = json
                println "Get File Got success response: ${resp.statusLine}"
//                println "Success Content-Type: ${resp.headers.'Content-Type'}"
//                resp.headers.each {
//                    println "success response header each ${it}"
//                }
            }

            response.failure = { resp ->
                println "Get file failure status: ${resp.statusLine}"
//                println "Get file, Failure Content-Type: ${resp.headers.'Content-Type'}"
                //println reader.text
            }


        }
    } catch (java.net.UnknownHostException t) {
        println "UnknownHostException Error trying to send   ${t}."
    } catch (org.apache.http.conn.HttpHostConnectException t) {
        println "HttpHostConnectException Error trying to show file  ${t}."
    } catch (javax.net.ssl.SSLPeerUnverifiedException t) {
        println "SSLPeerUnverifiedException Error trying to show file  ${t}"
    } catch (groovyx.net.http.ResponseParseException t) {

        String contentType
        try {
            contentType = t.response?.contentType
            println "content type: ${contentType}"
            println "parse exception: ${t}"
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
        }
        println "ResponseParseException Error trying to show file Parse exception. Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
        t.printStackTrace()
    } catch (groovyx.net.http.HttpResponseException t) {

        String contentType
        try {
            println "response exception ${t}"
            contentType = t.response?.contentType
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
            println "exception illegal arg ${e}"
        }
        println "HttpResponseException Error trying to show file. Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
    }

    // go off and print the file
    File testFile = new File(jobResults.fileName)
    if (testFile.isFile()){
        println "File does exist ${jobResults.fileName}"
        command = "${jobResults.printerCommand} ${jobResults.fileName}"
        println "command: ${command}"
        proc = command.execute(null, null)
        proc.waitFor()
        def lsResults = proc.in.text.readLines().collect {
            it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
        }
        println "print file ls results: ${lsResults}"
        updatePrintDate(encodedId, jobResults)

    }
    else {
        println "File does not exist ${jobResults.fileName}"
    }

}


def updatePrintDate(def encodedId, def jobResults) {
    def results = []
    try {


        def pendingPrint = new HTTPBuilder()
        pendingPrint.request("http://localhost:8080/", PUT, ContentType.JSON) { req ->
            uri.path = "/BannerGeneralSsb/api/jobsub-pending-print/${jobResults.id}"
            headers.Accept = "application/json"
            headers.ContentType = "application/json"
            headers.Authorization = "Basic ${encodedId}"
            body = [job: jobResults.job]
            response.success = { resp ->
                println "Update success response: ${resp.statusLine}"
//                println "Update Success Content-Type: ${resp.headers.'Content-Type'}"
//                resp.headers.each {
//                    println "update success response header each ${it}"
//                }

            }

            response.failure = { resp ->
                println "Update, Got failure response: ${resp.statusLine}"
                println "Update, Failure Content-Type: ${resp.headers.'Content-Type'}"
                resp.headers.each {
                    println "update failure response header each ${it}"
                }
                //println reader.text
            }


        }
    } catch (java.net.UnknownHostException t) {
        println "UnknownHostException Error trying to put update   ${t}."
    } catch (org.apache.http.conn.HttpHostConnectException t) {
        println "HttpHostConnectException Error trying to put update    ${t}."
    } catch (javax.net.ssl.SSLPeerUnverifiedException t) {
        println "SSLPeerUnverifiedException Error trying to put update    ${t}"
    } catch (groovyx.net.http.ResponseParseException t) {

        String contentType
        try {
            contentType = t.response?.contentType
            println "content type: ${contentType}"
            println "parse exception: ${t}"
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
        }
        println "ResponseParseException Error trying to put update  Parse exception. Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
    } catch (groovyx.net.http.HttpResponseException t) {

        String contentType
        try {
            println "response exception ${t}"
            contentType = t.response?.contentType
        } catch (IllegalArgumentException e) {
            contentType = e.getMessage()
            println "exception illegal arg ${e}"
        }
        println "HttpResponseException Error trying to put update Response content type = ${contentType}; status line = '${t.response?.statusLine}' ${t.message}"
    }


}









