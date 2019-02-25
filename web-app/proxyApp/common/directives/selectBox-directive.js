/*******************************************************************************
  Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
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

proxyAppDirectives.directive('genssbXeDropdown', ['$parse', '$filter', function($parse, $filter) {
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
        restrict: 'EA',
        scope: true,
        template: '<xe-ui-select ng-model="modelHolder[modelName]" on-select="onSelectFn()"\n' +
            '             reach-infinity="refreshData($select.search, true)" theme="select2" search-enabled="searchEnabled" ng-disabled="isDisabled">\n' +
            '   <xe-ui-select-match ng-init="setupSelectCtrlFocusser($select)" placeholder="{{selPlaceholder}}">\n' +
            '       {{$select.selected.description ? $select.selected.description : selPlaceholder}}\n' +
            '   </xe-ui-select-match>\n' +
            '   <xe-ui-select-choices minimum-input-length="" refresh-delay="200" repeat="item in selectItems"\n' +
            '                         refresh="refreshData($select.search)">\n' +
            '   <span ng-switch="isLoading">\n' +
            '       <span ng-switch-when="true"></span>\n' +
            '       <div ng-switch-default="any" ng-bind-html="item.description | highlight: $select.search"></div>\n' +
            '   </span>\n' +
            '   </xe-ui-select-choices>\n' +
            '</xe-ui-select>',
        link: function(scope, elem, attrs) {
            var curPage = 0, stopLoading = false, fetchFn = $parse(attrs.fetchFunction)(scope),
                initItemList = function() {
                    return (attrs.showNa === 'true' ? [{code: null, description: notApplicableText}] : []);
                },
                focusText = attrs.focusText;

            scope.modelHolder = $parse(attrs.modelHolder)(scope);
            scope.modelName = attrs.modelName;
            scope.onSelectFn = $parse(attrs.onSelectFn)(scope);
            scope.selPlaceholder = attrs.dropdownPlaceholder;
            scope.selectItems = initItemList();
            scope.isDisabled = $parse(attrs.isDisabled)(scope);
            scope.searchEnabled = attrs.searchEnabled ? attrs.searchEnabled === 'true' : true;

            scope.refreshData = function(search, loadingMore) {
                var isNewSearch = !loadingMore || (stopLoading && scope.selectItems.length === 0);
                if (isNewSearch) {
                    // new search
                    curPage = 0;
                    stopLoading = false;
                }

                if(!scope.isLoading && !stopLoading) {
                    if(loadingMore) {
                        // get more results from current search
                        curPage++;
                    }

                    scope.isLoading = true;
                    fetchFn({
                        searchString: search ? search : '',
                        offset: curPage,
                        max: 10
                    }).$promise.then(function (response) {
                        if(isNewSearch) {
                            scope.selectItems = initItemList();
                        }
                        _.each(response, function(item) {
                            item.description = getDescriptionFromAddressComponent(item);
                            scope.selectItems.push(item);
                        });
                        scope.isLoading = false;
                        if (response.length < 10) {
                            stopLoading = true; // we found everything
                        }
                    });
                }
            };

            scope.setupSelectCtrlFocusser = function($selectCtrl) {
                $selectCtrl.focusserTitle = focusText;
            };
        }
    };
}]);