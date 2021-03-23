/*******************************************************************************
 Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

globalProxyManagementApp.service('notificationCenterService', ['$filter', function ($filter) {
    const flashNotification = false;

    this.localMessageCenter = null;
    this.clearNotifications = function() {
        if (window.notifications !== undefined) {
            notifications.clearNotifications();
        }
    };

    /**
     * Display a single notification, clearing all existing notifications.
     * @param displayMessage
     * @param messageType
     * @param flashType
     * @param prompts
     */
    this.displayNotification = function(displayMessage, messageType, flashType, prompts) {
        const i18nDisplayMessage = $filter('i18n')(displayMessage);

        displayMessage = i18nDisplayMessage !== undefined ? i18nDisplayMessage : displayMessage;

        this.clearNotifications();

        const notification = new Notification(
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

        this.focusNotificationCenter(messageType);
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
        const i18nDisplayMessage = $filter('i18n')(displayMessage);
        let notification = null,
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
        else {
            notificationCenter.openNotificationFlyout();
        }

        if ( messageType === "info") {
            angular.element(".notification-center-flyout li.notification-item").addClass("notification-center-message-info");
        }

        this.focusNotificationCenter(messageType);
        return notification;
    };

    this.focusNotificationCenter = function(messageType) {
        let notifCenterElems;

        if(this.localMessageCenter !== null) {
            notifCenterElems = $(this.localMessageCenter);
            notifCenterElems.focus();
        }
        else if(messageType === "success") {
            notifCenterElems = $("div.notification-center-flyout > ul > li > div");
            notifCenterElems.attr("tabindex", 0);
            notifCenterElems.focus();
            notifCenterElems.removeAttr("tabindex");
        }
    };

    this.setLocalMessageCenter = function(elementSelector) {
        this.localMessageCenter = elementSelector;
    };

    this.removeNotification = function(notification) {
        // make sure argument is a Notification object, if it is not, then find the Notification object by
        // its message, then remove it
        if(notification instanceof Notification) {
            notifications.remove(notification);
        }
        else {
            const i18nDisplayMessage = $filter('i18n')(notification);

            const msg = i18nDisplayMessage !== undefined ? i18nDisplayMessage : notification;

            const identByMsg = _.find(notifications.models, function (n) {
                return n.get('message') === msg;
            });

            if(identByMsg) {
                notifications.remove(identByMsg);
            }
        }
    };

}]);
