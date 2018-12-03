/*******************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
proxyAppDirectives.directive('selectBox',['$filter', function($filter) {

    // Get description from an address field item, e.g. an item for a state or nation
    var getDescriptionFromAddressComponent = function(item) {
        if('webDescription' in item && item.webDescription) {
            return item.webDescription;
        }
        else if('nation' in item && item.nation) {
            return item.nation;
        }
        else {
            return item.description;
        }
    },
    notApplicableText = $filter('i18n')('personInfo.label.notApplicable');

    return {
        scope: true,
        link: function(scope, elem, attrs) {
            var data = angular.fromJson(attrs.forSelect),
                dataModelItem = scope.$eval(data.model),
                maxItems = 10,
                showNA = data.showNA;

            // set the element's title to interpolated value from the attributes
            if(elem.attr('title')) {
                elem.attr('title', attrs.title);
            }

            elem.select2({
                width: '100%',
                placeholder: data.placeholder,
                ajax: {
                    url: data.action,
                    dataType: 'json',
                    quietMillis: 800,
                    data: function(term, page) {
                        return  {
                            searchString: term,
                            offset: page-1,
                            max: maxItems
                        };
                    },
                    cache: true,
                    allowClear: true,
                    results: function(data, page) {
                        var results = (showNA && page === 1) ? [{id: 'not/app', text: notApplicableText}] : [],
                            more = false;

                        if(!data.failure) {
                            $.each(data, function(i, item) {
                                results.push({
                                    id: item.code,
                                    text: getDescriptionFromAddressComponent(item)
                                });
                            });
                            more = (page * 1) < data.length;
                        }

                        return {
                            results: results,
                            more: more
                        };
                    }
                },
                formatSelection: function(item) {
                    if(showNA && item.id === 'not/app') {
                        dataModelItem.code = '';
                        dataModelItem.description = '';
                    }
                    else {
                        dataModelItem.code = item.id;
                        dataModelItem.description = item.text;
                    }

                    return item.text;
                },
                initSelection: function(element, callback) {
                    if (dataModelItem) {
                        var data = {id: dataModelItem.code, text: getDescriptionFromAddressComponent(dataModelItem)};

                        callback(data);
                    }
                }
            }).select2("val", "_"); // Dummy value needed to make initSelection do its thing

            if(data.disableOnUpdate && !scope.isCreateNew) {
                elem.select2("enable", false);
            }
        }
    };
}]);


proxyAppDirectives.directive('legalSexSelect', function() {

    return {
        scope: {
            profileItem: '='
        },
        link: function(scope, elem, attrs) {
            elem.select2({
                width: '100%',
                minimumResultsForSearch: -1, // Hide search box
                query: function(query) {
                    var data = {results: []};

                    data.results.push({id: 'M', text: 'Male'});
                    data.results.push({id: 'F', text: 'Female'});
                    data.results.push({id: 'N', text: 'Unknown'});

                    query.callback(data);
                },
                formatSelection: function(item) {
                    scope.profileItem.model = item.id;

                    return item.text;
                },
                initSelection: function(element, callback) {
                    var selection = scope.profileItem.model === 'M' ? 'Male' : scope.profileItem.model === 'F' ? 'Female': 'Unknown';
                    var data = {id: scope.profileItem.model, text: selection};

                    callback(data);
                }
            });
        }
    };
});
