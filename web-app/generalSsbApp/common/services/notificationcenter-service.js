/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('notificationCenterService', ['$filter', function ($filter) {
    var flashNotification = false;
    this.clearNotifications = function() {
        notifications.clearNotifications();
    };
    this.displayNotifications = function(displayMessage, messageType, flashType) {
        if ($filter('i18n')(displayMessage) !== undefined) {
            displayMessage = $filter('i18n')(displayMessage);
        }
        this.clearNotifications();
        var notification = new Notification(
            {
                message: displayMessage,
                type: messageType,
                model: null,
                flash: flashType ? flashType : flashNotification,
                attribute: null
            }
        );
        notifications.addNotification(notification);
    };
}]);