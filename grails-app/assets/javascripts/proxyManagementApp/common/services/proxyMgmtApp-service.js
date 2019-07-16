/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

proxyManagementApp.service('proxyMgmtAppService', ['$rootScope', '$filter', '$resource', '$q', function ($rootScope, $filter, $resource, $q) {

    // TODO: DELETE THIS MOCK DATA
    var mockCommunicationLogData = [
        {
            transmitDate: '01/11/2019',
            subject: 'New proxy confirmation 1',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '02/11/2019',
            subject: 'New proxy confirmation 2',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '03/11/2019',
            subject: 'New proxy confirmation 3',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '04/11/2019',
            subject: 'New proxy confirmation 4',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '05/11/2019',
            subject: 'New proxy confirmation 5',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '06/11/2019',
            subject: 'New proxy confirmation 6',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '07/11/2019',
            subject: 'New proxy confirmation 7',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '08/11/2019',
            subject: 'New proxy confirmation 8',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '09/11/2019',
            subject: 'New proxy confirmation 9',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '10/11/2019',
            subject: 'New proxy confirmation 10',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '11/11/2019',
            subject: 'New proxy confirmation 11',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '12/11/2019',
            subject: 'New proxy confirmation 12',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '01/22/2019',
            subject: 'New proxy confirmation 13',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '02/22/2019',
            subject: 'New proxy confirmation 14',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '03/22/2019',
            subject: 'New proxy confirmation 15',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '04/22/2019',
            subject: 'New proxy confirmation 16',
            actionDate: '04/18/2019',
            expirationDate: '02/08/2020',
            resend: 5
        },
        {
            transmitDate: '05/22/2017',
            subject: 'Send proxy pin reset request 17',
            actionDate: '06/10/2019',
            expirationDate: '06/06/2021',
            resend: 5
        }
    ];
    // END MOCK DATA

    var fetchProxies = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxies'}, {query: {method:'GET', isArray:false}}),
        fetchRelationshipOptions = $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getRelationshipOptions'}, {query: {method:'GET', isArray:false}});


    this.getProxyList = function () {
        return fetchProxies.query();
    };

    this.getProxy = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getProxy'}).get(params);
    };

    this.deleteProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'deleteProxy'}, {delete: {method:'POST'}}).delete(entity);
    };

    this.createProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'createUpdateProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.updateProxy = function (entity) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'updateProxy'}, {delete: {method:'POST'}}).save(entity);
    };

    this.getDataModelOnRelationshipChange = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getDataModelOnRelationshipChange'}).get(params);
    };

    this.getRelationshipOptions = function () {
        return fetchRelationshipOptions.query();
    };

    this.resetProxyPassword = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'resetProxyPassword'}).get(params);
    };

    this.getClonedProxiesList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxiesList'}).get(params);
    };

    this.getClonedProxiesListOnCreate = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxiesListOnCreate'}).get(params);
    };

    this.getClonedAuthorizationsList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getDataModelOnAuthorizationChange'}).get(params);
    };

    this.getAddProxiesList = function (params) {
        return $resource('../ssb/:controller/:action',
            {controller: 'ProxyManagement', action: 'getClonedProxyAddList'}).get(params);
    };

    // OPTION 1: TRADITIONAL PAGINATION
    // TO USE THIS, ALSO SET continuous-scrolling="false" IN proxyMgmtCommunication.html
    //===================================================================================
    this.getCommunicationLog = function (params) {
        var deferred = $q.defer(),
            result = {result:[], length: mockCommunicationLogData.length},
            i, logEntry;

        if (params.offset < mockCommunicationLogData.length) {
            for (i=params.offset; i < params.max; i++) {
                logEntry = mockCommunicationLogData[i];

                if (logEntry) {
                    result.result.push(logEntry);
                } else {
                    break;
                }
            }
        }

        deferred.resolve(result);

        return deferred;
    };

    // OPTION 2: CONTINUOUS SCROLLING
    // TO USE THIS, ALSO SET continuous-scrolling="true" IN proxyMgmtCommunication.html
    //===================================================================================
    // this.getCommunicationLog = function (params) {
    //     var deferred = $q.defer(),
    //         result = {result:mockCommunicationLogData, length: mockCommunicationLogData.length};
    //
    //     deferred.resolve(result);
    //
    //     return deferred;
    // };

}]);
