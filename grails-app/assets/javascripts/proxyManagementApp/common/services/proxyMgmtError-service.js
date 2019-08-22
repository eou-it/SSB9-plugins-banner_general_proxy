proxyManagementApp.service('proxyMgmtErrorService', ['notificationCenterService', '$filter',
    function (notificationCenterService, $filter) {

        var dateFmt,
            calendar = (function(){
                var locale = window.i18n.locale;

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
            validEmailRegEx = /[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+@[^ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]+\.[A-Z]{2,}/i,

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

        this.refreshMessages = function() {
            messages = [];
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
            var msg = 'proxyManagement.onSave.BADEMAIL';
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

        var dateFieldsAreEmpty = function (proxy) {
                return (!proxy.p_start_date || !proxy.p_stop_date);
            },
            stopDateIsBeforeStartDate = function (proxy) {
                var MAX_DATE = 8640000000000000,
                    fromDate = stringToDate( proxy.p_start_date ),
                    toDate = proxy.p_stop_date ? stringToDate(proxy.p_stop_date) : new Date(MAX_DATE);
                return fromDate > toDate;
            },
            datesFormatsAreInvalid = function (proxy) {
                return !stringToDate(proxy.p_start_date) || !stringToDate(proxy.p_stop_date);
            },

            currentErrorDateNotification,

            removeDateErrors = function () {
                notificationCenterService.removeNotification('personInfo.address.error.dateFormat');
                notificationCenterService.removeNotification('proxy.personalinformation.onSave.required_data_missing');
                notificationCenterService.removeNotification(currentErrorDateNotification);
            };

        /*1) Checks if BOTH date fields have data.
        * 2) Checks if the dates are correctly formatted
        * 3) Checks if the stop date is before the start date*/
        this.getErrorDates = function(proxy) {
            var msg = 'personInfo.address.error.dateFormat';

            //Removes any existing errors so errors that are no longer true do not stay showing.
            removeDateErrors();

            if (dateFieldsAreEmpty(proxy)) {
                msg = 'proxy.personalinformation.onSave.required_data_missing';
                messages.push({msg: $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]), type: 'error'});
                return $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]);
            }
            else if (datesFormatsAreInvalid(proxy)) {
                messages.push({msg: $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]), type: 'error'});
                return $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]);
            }
            else if (stopDateIsBeforeStartDate(proxy)) {
                /*Because the stop date before start date error is dynamically generated for each occurrence of the error,
                * we need to store the previous notification so that the notificationCenterService is not trying to remove
                * an error based on the current dates in the date fields.*/
                msg = 'proxyManagement.message.checkDates';
                if (currentErrorDateNotification) {
                    removeDateErrors(msg)
                }
                currentErrorDateNotification = notificationCenterService.addNotification($filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]), 'error');
                return $filter('i18n')(msg,[proxy.p_start_date, proxy.p_stop_date]);
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
