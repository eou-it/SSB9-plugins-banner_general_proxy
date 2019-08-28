proxyManagementApp.service('proxyMgmtDateService', ['$filter',
    function ($filter) {
        var dateFmt,
            calendar = (function(){
                var locale = window.i18n.locale;

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

    }
]);
