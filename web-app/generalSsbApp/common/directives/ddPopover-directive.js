/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

generalSsbAppDirectives.directive('popOver', [function() {

    var template = '<button class="icon-info-CO" data-toggle="popover"></button>';

    var link = function (scope, element, attrs) {
        $(element).on('click', function(e) {
            var id = attrs.popoverId || '',
                popovers = scope.popoverElements,
                popoverElement = popovers[id],
                src = attrs.popoverImg || '';

            // Close all open popovers, except for the currently requested one if it's already open.
            // And if the currently requested one is already open, grab a reference to it and don't close it.
            for (var popoverID in popovers) {
                if (popovers.hasOwnProperty(popoverID)) {
                    if (popoverID === id) {
                        popoverElement = popovers[popoverID];
                    } else {
                        $(popovers[popoverID]).popover('hide');
                    }
                }
            }

            if (!popoverElement) {
                popoverElement = $(element).popover({
                    content: '<img class="sample-check" src="' + src + '">',
                    trigger: 'manual',
                    placement: 'left',
                    html: true
                });

                popovers[id] = popoverElement;
            }

            $(popoverElement).popover('show');
        });
    };

    return {
        restrict: 'E',
        template: template,
        link : link
    };
}]);

generalSsbAppDirectives.directive('hidePopover', [function(){
    var link = function(scope, element) {
        $(element).on('click', function(e) {
            // Any element that has 'data-toggle="popover"' cannot be used to hide it.  At the time of this writing,
            // that's used for the icon (because clicking that to open the popover would otherwise immediately close
            // it right here) and for the popover proper.
            if ($(e.target).data('toggle') !== 'popover' && $(e.target).parents('.popover.in').length === 0) {
                hide(scope.popoverElements);
            }
        });

        function hide(popovers){
            for (var popoverID in popovers) {
                if (popovers.hasOwnProperty(popoverID)) {
                    $(popovers[popoverID]).popover('hide');
                }
            }
        }
    };

    return {
        restrict: ' A',
        link: link
    };
}]);
