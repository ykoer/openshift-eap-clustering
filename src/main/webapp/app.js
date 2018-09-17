var myApp = angular.module('ClusteringTestApp', ['ui.bootstrap', 'ui.router', 'ClusteringTestApp.controllers']);

myApp.config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    $urlRouterProvider.otherwise('session');

    $stateProvider
        .state('session', {
            url: '/session',
            templateUrl: 'views/session.html',
            controller: 'SessionController'
        })
        .state('openshift', {
            url: '/openshift',
            templateUrl: 'views/openshift.html',
            controller: 'OpenshiftController'
        })
});

myApp.controller("tabsController", function ($rootScope, $scope, $state) {
    
    $scope.tabs = [
        { title: "Session", route: "session", active: true },
        { title: "Openshift", route: "openshift", active: true }
    ];

});
