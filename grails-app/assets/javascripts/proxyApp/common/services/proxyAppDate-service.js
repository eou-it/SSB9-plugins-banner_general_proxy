proxyApp.service('proxyAppDateService', ['$filter',
    function ($filter) {
        var dateFmt,
            calendar = (function(){
                var locale = $('meta[name=locale]').attr("content");

                if(locale.split('-')[0] === 'ar') {
                    dateFmt = $filter('i18n')('default.date.format');
                    return $.calendars.instance('islamic');
                }
                else {
                    dateFmt = $filter('i18n')('default.date.format').toLowerCase();
                    dateFmt = dateFmt.replace(/mmm/i, 'M'); // short month format is M, not MMM, for jQuery calendar
                    return $.calendars.instance();
                }
            }());


        this.stringToDate = function (date) {
            var result;
            try {
                result = calendar.parseDate(dateFmt, date).toJSDate();
                return result;
            }
            catch (exception) {
                return null;
            }
        };

        this.dateIsInPast = function (date) {
            var now = new Date();
            return this.stringToDate(date) < now;
        }

    }
]);
