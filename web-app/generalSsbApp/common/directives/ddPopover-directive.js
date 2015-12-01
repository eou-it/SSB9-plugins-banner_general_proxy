/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppDirectives.directive('popOver', ['directDepositService', function(directDepositService) {

    var template = '<button class="icon-info-CO"></button>';

    var link = function (scope, element, attrs) {
        $(element).on('click', function(e) {
            var src = attrs.popoverImg || '';

            // Prevent the hidePopover directive from handling the event, immediately closing the popover
            e.stopImmediatePropagation();

            // Toggle popover open/closed
            if ($(element).next('.popover.in').length !== 0) {
                // Popover is already open, toggle it closed
                $(element).popover('destroy');
            } else {
                // Destroy any existing popovers
                directDepositService.destroyAllPopovers();

                // Open popover
                $(element).popover({
                    content: '<img class="sample-check" src="' + src + '">',
                    trigger: 'manual',
                    placement: 'bottom',
                    html: true
                });

                // Adjust positioning, etc. based on screen dimensions
                // TODO: make this work for other dimensions than just portrait mobile
                $(element).on('shown.bs.popover', function(e) {
                    var flipsLeft = isElementRightOfCenter(e.target),
                        flipClass = flipsLeft ? 'flipleft' : 'flipright',
                        popoverElement = $(e.target).next(),
                        leftOffset = flipsLeft ? 150 : 161;

                    popoverElement.css('left', parseInt($(popoverElement).css('left')) + leftOffset + 'px');
                    popoverElement.css('top', parseInt($(popoverElement).css('top')) - 10 + 'px');
                    popoverElement.find('.popover-content').css('padding', '9px');
                    popoverElement.addClass(flipClass);
                });

                $(element).popover('show');
            }
        });
    };

    function isElementRightOfCenter(element) {
        var parentCenter = $(element).parent().width() / 2,
            iconRightEdgePos = $(element).offset().left,
            iconCenterPos = iconRightEdgePos - ($(element).width() / 2);

        return iconCenterPos > parentCenter;
    }

    return {
        restrict: 'E',
        template: template,
        link : link
    };
}]);

generalSsbAppDirectives.directive('hidePopover', ['directDepositService', function(directDepositService) {
    var link = function(scope, element) {
        $(element).on('click', function(e) {
            // If not clicking on the modal (the check for ".parents('.popover.in')" checks for that),
            // destroy all popovers.
            if ($(e.target).parents('.popover.in').length === 0) {
                directDepositService.destroyAllPopovers();
            }
        });
    };

    return {
        restrict: 'A',
        link: link
    };
}]);
