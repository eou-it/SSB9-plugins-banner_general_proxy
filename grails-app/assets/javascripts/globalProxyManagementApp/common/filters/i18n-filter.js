/********************************************************************************
  Copyright 2020 Ellucian Company L.P. and its affiliates.
********************************************************************************/
angular.module('I18n', []).
    filter('i18n', function () {
        return function (key, data) {
            var value = _.isUndefined(key) ? key : window.i18n[key];
            if(value && data) {
                for(var i=0; i < data.length; i++) {
                    var regexp = new RegExp('\\{('+i+')\\}', "g");

                    // Note how the replacement value (second argument to the "replace" function) is wrapped inside an
                    // anonymous function. Historically, this hasn't been done in i18n filters.  However, where the
                    // replacement value is a dollar value, a problem can occur where the "$1" in $16,500.00, for
                    // example, is viewed by the "replace" function as a string submatch pattern. Where the target
                    // string is "the cost is {0}", the outcome can be something like "the cost is 06,500.00".  Wrapping
                    // that value in a function gets around that problem.
                    value = value.replace(regexp, function() {
                        return data[i] !== undefined ? data[i] : ''
                    });
                }
            }
            return value
        };
    });
