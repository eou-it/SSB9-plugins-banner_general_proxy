/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
proxyManagementApp.factory('ProxyManagementDateErrorManager', ['proxyMgmtDateService', 'notificationCenterService', '$filter',
    function (proxyMgmtDateService, notificationCenterService, $filter) {
        var ERRORS = [
                "START_EMPTY_FIELD",
                "STOP_EMPTY_FIELD",
                "START_INVALID_DATE",
                "STOP_INVALID_DATE",
                "STOP_BEFORE_START"
            ],
            START_DATE_ERRORS = [
                ERRORS[0],
                ERRORS[2],
                ERRORS[4]
            ],
            STOP_DATE_ERRORS = [
                ERRORS[1],
                ERRORS[3],
                ERRORS[4]
            ],
            removeDateErrors = function (currentErrorDateNotification) {
                notificationCenterService.removeNotification('personInfo.address.error.dateFormat');
                notificationCenterService.removeNotification('proxy.personalinformation.onSave.required_data_missing');
                notificationCenterService.removeNotification(currentErrorDateNotification);
            },
            getDateEmptyErrors = function (startDate, stopDate) {
                var errors = [];
                if (proxyMgmtDateService.dateFieldIsEmpty(startDate)) {
                    errors.push(ERRORS[0])
                }
                if (proxyMgmtDateService.dateFieldIsEmpty(stopDate)) {
                    errors.push(ERRORS[1])
                }
                return errors;
            },
            getInvalidDateFormatErrors = function (startDate, stopDate) {
                var errors = [];
                if (proxyMgmtDateService.dateFormatIsInvalid(startDate)) {
                    errors.push(ERRORS[2])
                }
                if (proxyMgmtDateService.dateFormatIsInvalid(stopDate)) {
                    errors.push(ERRORS[3])
                }
                return errors;
            },
            getMessageKeyFromError = function (error) {
                if (error === ERRORS[0] || error === ERRORS[1]) {
                    return 'proxy.personalinformation.onSave.required_data_missing';
                } else if (error === ERRORS[2] || error === ERRORS[3]) {
                    return 'personInfo.address.error.dateFormat';
                } else if (error === ERRORS[4]) {
                    return 'proxyManagement.message.checkDates';
                } else {
                    return '';
                }
            },
            getNotificationsFromErrors = function (errors) {
                var notifications = [],
                errorsLength = errors.length;
                for (var i = 0; i < errorsLength; i++) {
                    notifications.push(getMessageKeyFromError(errors[i]))
                }
                return notifications;
            },
            getMessageFromError = function (error, startDate, stopDate) {
                return ($filter('i18n')(getMessageKeyFromError(error), [startDate, stopDate]))
            };
        return function () {
            return {
                currentErrors: [],
                currentNotification: '',
                startDate: '',
                stopDate: '',
                startAndStopWereModified: false,
                setDates: function (startDate, stopDate) {
                    this.startDate = startDate;
                    this.stopDate = stopDate;
                    if (startDate && stopDate) {
                        this.startAndStopWereModified = true;
                    }
                },
                setErrorMessages: function (isSubmit) {
                    this.currentErrors = [];
                    removeDateErrors(this.currentNotification);
                    if (this.startAndStopWereModified || isSubmit) {
                        this.currentErrors = this.currentErrors.concat(getDateEmptyErrors(this.startDate, this.stopDate));
                    }
                    this.currentErrors = this.currentErrors.concat(getInvalidDateFormatErrors(this.startDate, this.stopDate));
                    if (this.currentErrors.length === 0) {
                        if (proxyMgmtDateService.stopDateIsBeforeStartDate(this.startDate, this.stopDate)) {
                            this.currentErrors.push(ERRORS[4]);
                        }
                    }
                },
                displayNotifications: function () {
                    var notifications = getNotificationsFromErrors(this.currentErrors),
                    notificationsLength = notifications.length;
                    for (var i = 0; i < notificationsLength; i++) {
                        this.currentNotification = notificationCenterService.addNotification($filter('i18n')(notifications[i], [this.startDate, this.stopDate]), 'error');
                    }
                },
                getStartDateErrorMessage: function () {
                    var error;
                    error = this.currentErrors.filter(function (err) {
                        if (START_DATE_ERRORS.indexOf(err) !== -1) {
                            return err;
                        }
                    });
                    return getMessageFromError(error[0], this.startDate, this.stopDate);
                },
                getStopDateErrorMessage: function () {
                    var error;
                    error = this.currentErrors.filter(function (err) {
                        if (STOP_DATE_ERRORS.indexOf(err) !== -1) {
                            return err;
                        }
                    });
                    return getMessageFromError(error[0], this.startDate, this.stopDate);
                }
            }
        }
    }]);
