generalSsbAppControllers.controller('gssLandingPageController',['$scope',
    function ($scope) {

        // CONTROLLER VARIABLES
        // --------------------
        $scope.appTiles = [
            {
                title: 'banner.generalssb.landingpage.personalinfo.title',
                desc: 'banner.generalssb.landingpage.personalinfo.description',
                url: '/BannerGeneralSsb/ssb/personalInformation',
                icon: '../images/personal_info.svg'
            },
            {
                title: 'banner.generalssb.landingpage.directdeposit.title',
                desc: 'banner.generalssb.landingpage.directdeposit.description',
                url: '/BannerGeneralSsb/ssb/directDeposit',
                icon: '../images/direct_deposit.svg'
            }
        ];
    }
]);
