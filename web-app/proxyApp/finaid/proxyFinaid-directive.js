/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
// proxyAppDirectives.directive('textWithLinks', [function () {
//     return{
//         restrict: 'E',
//         template: '<div ng-if=textData>\n' +
//             '            <span ng-repeat="textSegment in textData">\n' +
//             '                <span ng-if="textSegment.url"><a href="{{textSegment.url}}">{{textSegment.text}}</a></span>\n' +
//             '                <span ng-if="!textSegment.url">{{textSegment.text}}</span>\n' +
//             '            </span>\n' +
//             '      </div>',
//         scope: {
//             textData: '='
//         }
//     };
// }]);
proxyAppDirectives.directive('textWithLinks', [function () {
    return{
        restrict: 'E',
        template: '<span ng-repeat="textSegment in textData">\n' +
            '          <span ng-if="textSegment.url" class="proxy-base-font"><a ui-sref="{{textSegment.url}}()">{{textSegment.text}}</a></span>\n' +
            '          <span ng-if="!textSegment.url" class="proxy-base-font">{{textSegment.text}}</span>\n' +
            '      </span>',
        scope: {
            textData: '='
        }
    };
}]);
