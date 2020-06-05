/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.factory('ProxyManagementProxy',
    function () {
        return function (proxyProfile) {
            return {
                alt: proxyProfile ? proxyProfile.alt : null,
                cver : proxyProfile ? proxyProfile.cver : null,
                p_code: null,
                p_email: null,
                p_email_verify: null,
                p_last: null,
                p_first: null,
                p_desc: proxyProfile ? proxyProfile.p_desc : null,
                p_passphrase: proxyProfile ? proxyProfile.p_passphrase : null,
                p_retp_code: proxyProfile ? proxyProfile.p_retp_code : null,
                p_start_date: proxyProfile ? proxyProfile.p_start_date : null,
                p_stop_date: proxyProfile ? proxyProfile.p_stop_date : null,
                pages: proxyProfile ? proxyProfile.pages : [],

                getAuthorizedPages: function () {
                    return this.pages.filter(function (item) {
                        return item.auth === true;
                    });
                },

                handleAddListChange: function (proxyAuxData) {
                    this.p_code = proxyAuxData.addProxy.code;
                    this.cver = proxyAuxData.addProxy.cver;
                    this.p_email = proxyAuxData.addProxy.email;
                    this.p_last = proxyAuxData.addProxy.lastName;
                    this.p_first = proxyAuxData.addProxy.firstName;
                },

                handleRelationshipChange: function (response, proxyAuxData) {
                    this.p_start_date = response.dates.startDate;
                    this.p_stop_date = response.dates.stopDate;
                    this.pages = response.pages.pages;
                    this.p_retp_code = proxyAuxData.selectedRelationship.code;
                },

                toggleCheckboxes: function () {
                    this.pages.forEach(function (page) {
                        page.auth = event.target.checked;
                    });
                }

            }
        }
    });
