/*******************************************************************************
  Copyright 2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
proxyAppDirectives.directive('enterKey', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.enterKey);
                });

                event.preventDefault();
            }
        });
    };
});
