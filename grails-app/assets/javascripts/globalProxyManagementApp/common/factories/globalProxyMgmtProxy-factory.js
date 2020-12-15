/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
globalProxyManagementApp.factory('GlobalProxyManagementProxy',
    function () {
        return function (proxyProfile) {
            return {
                p_code: null,
                p_retp_code: proxyProfile ? proxyProfile.p_retp_code : null,
                pages: proxyProfile ? proxyProfile.pages : [],
                isValidTarget: 'false',
                isValidBannerId: 'false',
                targetId: '',
                preferredName: '',

                getAuthorizedPages: function () {
                    return this.pages.filter(function (item) {
                        return item.auth === true;
                    });
                },

                handleRelationshipChange: function (response, proxyAuxData) {
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
