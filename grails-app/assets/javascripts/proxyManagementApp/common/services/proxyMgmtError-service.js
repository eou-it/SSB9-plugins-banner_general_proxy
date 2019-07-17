proxyManagementApp.service('proxyMgmtErrorService', ['notificationCenterService', '$filter',
    function (notificationCenterService, $filter) {

        var dateFmt,
            calendar = (function(){
                var locale = $('meta[name=locale]').attr("content");
                if(locale.split('-')[0] === 'ar') {
                    dateFmt = $filter('i18n')('default.date.format');
                    return $.calendars.instance('islamic');
                }
                else {
                    dateFmt = $filter('i18n')('default.date.format').toLowerCase();
                    return $.calendars.instance();
                }
            }());

        var messages = [],
            proxyProfileMessageCenter = "#proxyProfileErrorMsgCenter",
            invalidCharRegEx = /[ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]/i,
            validEmailRegEx = /[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+@[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+\.[A-Z]{2,}/i;


        stringToDate = function (date) {
            var result;
            try {
                result = calendar.parseDate(dateFmt, date).toJSDate();
                return result;
            }
            catch (exception) {
                return null;
            }
        };

        this.getErrorFirstName = function(proxy) {
            var msg = 'proxyManagement.message.firstNameRequired';

            if (!proxy.p_first) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorLastName = function(proxy) {
            var msg = 'proxyManagement.message.lastNameRequired';

            if (!proxy.p_last) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorEmail = function(proxy) {
            var msg = 'proxyManagement.message.emailRequired';

            if (!proxy.p_email) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }

            return this.getErrorEmailAddressFormat(proxy);
        };

        this.getErrorEmailAddressFormat = function (proxy) {
            var msg = 'personInfo.email.error.emailAddressFormat';
            if (invalidCharRegEx.test(proxy.p_email)) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }

            return this.getErrorEmailAddressValid(proxy);
        };

        this.getErrorEmailAddressValid = function (proxy) {
            var msg = 'personInfo.email.error.emailAddressValid';
            if (!validEmailRegEx.test(proxy.p_email)) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorVerifyEmail = function(proxy) {
            var msg = 'proxyManagement.message.verifyEmailMustMatch';

            if (proxy.p_email !== proxy.p_email_verify) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorRelationship = function(proxy) {
            var msg = 'proxyManagement.message.relationshipRequired';

            if (!proxy.p_retp_code) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorAuthorizations = function(proxy) {
            var msg = 'proxyManagement.message.authorizedPageRequired',
                found,
                isAtLeastOnePageAuthorized = false;

            if (proxy.pages) {
                found = _.find(proxy.pages, function (page) {
                    return page.auth;
                });

                isAtLeastOnePageAuthorized = !!found;
            }

            if (!isAtLeastOnePageAuthorized) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };


        this.getErrorDates = function(proxy) {
            if (proxy.p_start_date && proxy.p_stop_date) {
                var msg = 'proxyManagement.message.checkDates',
                    MAX_DATE = 8640000000000000,
                    fromDate = stringToDate(proxy.p_start_date),
                    toDate = proxy.p_stop_date ? stringToDate(proxy.p_stop_date) : new Date(MAX_DATE)
                if (fromDate > toDate) {
                    messages.push({msg: $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]), type: 'error'});
                    return $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]);
                } else {
                    notificationCenterService.removeNotification(msg);
                }
            }

        };


        this.displayMessages = function() {
            notificationCenterService.setLocalMessageCenter(proxyProfileMessageCenter);

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(proxyProfileMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
