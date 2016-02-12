/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbApp.service('notificationCenterService', ['$filter', function ($filter) {
    var flashNotification = false;
    this.clearNotifications = function() {
        notifications.clearNotifications();
    };

    /**
     * Display a single notification, clearing all existing notifications.
     * @param displayMessage
     * @param messageType
     * @param flashType
     * @param prompts
     */
    this.displayNotification = function(displayMessage, messageType, flashType, prompts) {
        var i18nDisplayMessage = $filter('i18n')(displayMessage);

        displayMessage = i18nDisplayMessage !== undefined ? i18nDisplayMessage : displayMessage;

        this.clearNotifications();

        var notification = new Notification(
            {
                message: displayMessage,
                type: messageType,
                model: null,
                flash: flashType ? flashType : flashNotification,
                attribute: null,
                prompts: prompts
            }
        );
        notifications.addNotification(notification);
    };

    /**
     * Add a new notification to the existing notifications.
     * @param displayMessage
     * @param messageType
     * @param flashType
     * @param prompts
     * @returns {*} A reference to the newly added notification.  Can be used as a handle to remove it.
     */
    this.addNotification = function(displayMessage, messageType, flashType, prompts) {
        var i18nDisplayMessage = $filter('i18n')(displayMessage),
            notification = null,
            alreadyDisplayed;

        displayMessage = i18nDisplayMessage !== undefined ? i18nDisplayMessage : displayMessage;

        // Make sure message is not already displayed
        alreadyDisplayed = _.find(notifications.models, function(n) {
            return n.get('message') === displayMessage;
        });

        if (!alreadyDisplayed) {
            notification = new Notification(
                {
                    message: displayMessage,
                    type: messageType,
                    model: null,
                    flash: flashType ? flashType : flashNotification,
                    attribute: null,
                    prompts: prompts
                }
            );

            notifications.addNotification(notification);
        }

        return notification;
    };
}]);