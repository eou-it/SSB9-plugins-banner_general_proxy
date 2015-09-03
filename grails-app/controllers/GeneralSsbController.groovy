/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */


import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.LocalizeUtil

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

/* DJD imports for my local testing */

import net.hedtech.banner.general.overall.DirectDepositAccount
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.security.BannerGrantedAuthorityService


class GeneralSsbController  {

    def log = Logger.getLogger( this.getClass() )
    static defaultAction = "landingPage"
    def directDepositAccountService


    def fetchDate() {
        def map = ['date': new Date(), 'dateFormat': LocalizeUtil.dateFormat]
        render map as JSON
    }


    def landingPage() {
        try {
            //TODO: call fetch roles
            def model = [:]
            def url = g.message( code: 'default.url.landing.page.for.roles' )

            model = ['url': url]
            render model: model, view: "generalSsb"
        } catch (ApplicationException e) {
            render returnFailureMessage( e ) as JSON
        }
    }


    def  returnFailureMessage(ApplicationException  e) {
        def model = [:]
        model.failure = true
        log.error(e)
        try {
            model.message = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) }).message
            return model
        } catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }

    /* DJD action for my local testing */
    def dirdAccounts() {
        def model = [:]
        def pidm = BannerGrantedAuthorityService.getPidm()
        
        log.warn "Employee pidm = $pidm"
        
        model = DirectDepositAccount.fetchByPidm(pidm)
        //model = DirectDepositAccount.findByPidm(pidm)
        
        log.warn "Employee model = $model.size()"

        render model
    }
    
    /* DJD action for my local testing */
    def newAcct() {
        def model = [:]
        def pidm = BannerGrantedAuthorityService.getPidm()
        
        def acct = new DirectDepositAccount(
            pidm: pidm,
            status: "A",
            documentType: "D",
            priority: 16,
            apIndicator: "A",
            hrIndicator: "I",
//          lastModified: $lastModified,
//          lastModifiedBy: $lastModifiedBy,
            bankAccountNum: 36958575,
            bankRoutingNum: 123478902,
            amount: null,
            percent: 100.0,
            accountType: "C",
//          addressTypeCode: $addressTypeCode,
//          addressSequenceNum: $addressSequenceNum,
            intlAchTransactionIndicator: "N"
//          isoCode: $isoCode,
//          apAchTransactionTypeCode: $apAchTransactionTypeCode
//          iatAddressTypeCode: $iatAddressTypeCode
//          iatAddessSequenceNum: $iatAddessSequenceNum
        )
        
        //def newMap = [domainModel: acct]

        directDepositAccountService.create([domainModel: acct], true)
        //DirectDepositAccountService.create(acct)
        //acct.save()

        model = DirectDepositAccount.fetchByPidm(pidm)

        render model
    }
    
    /* DJD action for my local testing */
    def delAcct() {
        def pidm = BannerGrantedAuthorityService.getPidm()
        def model = [:]
        def acct = DirectDepositAccount.get(160);
        
        log.warn "Employee acct = $acct"
        
        //good - directDepositAccountService.delete(acct)
        
        model = DirectDepositAccount.fetchByPidm(pidm)
        
        render model
    }
    
    /* DJD action for my local testing */
    def apAccts() {
        def model = [:]
        def pidm = BannerGrantedAuthorityService.getPidm()
        
        model = DirectDepositAccount.fetchByPidmAndApIndicator(pidm)

        render model
    }
    
    /* DJD action for my local testing */
    def fetchAcct() {
        def model = [:]
        def pidm = BannerGrantedAuthorityService.getPidm()
        def bankRoutingNum = "123478902"
        def bankAccountNum = "29149281945"
        def accountType = "S"
        
        model = DirectDepositAccount.fetchByPidmAndAccountInfo(pidm, bankRoutingNum, bankAccountNum, accountType)

        render model
    }

}
