angular.module("ClusteringTestApp.controllers", ['ClusteringTestApp.services'])

.controller('OpenshiftController', ['$scope', '$state', '$stateParams', 'Notifications', 'OpenshiftService', function($scope, $state, $stateParams, Notifications, OpenshiftService) {


    $scope.token = "";

    $scope.login = function() {
        OpenshiftService.login.put({
            token: $scope.token
        });
    }

    $scope.getProjectPods = function() {
        OpenshiftService.pods.query({
            project: "ads--prototype",
            service: "eap-clustering-test"
        }, function(data) {
            $scope.pods = data;
            console.log(data);
        });
    }

    $scope.deletePod = function(pod) {
        pod.$delete({
            project: "ads--prototype"
        }, function() {
            $scope.getProjectPods();
        });
    }
}])

.controller('SessionController', ['$scope', '$state', '$stateParams', 'Notifications', 'SessionAttribute', function($scope, $state, $stateParams, Notifications, SessionAttribute) {


    $scope.newAttribute = new SessionAttribute;

    $scope.setAttribute = function() {

        SessionAttribute.save($scope.newAttribute, function(data) {
            $scope.allAttributes = data.attributes;
            $scope.node = data.node;
            $scope.sessionid = data.sessionId;
            $scope.newAttribute = new SessionAttribute;
        });

    }      

    $scope.setAttribute2 = function() {
        $scope.newAttribute.$save()
        .then(function(data){
            $scope.allAttributes = data.attributes;
            $scope.node = data.node;
            $scope.sessionid = data.sessionId;
        });
    }

    $scope.getAttributes = function() {
        SessionAttribute.query(function(data) {
            $scope.allAttributes = data.attributes;
            $scope.node = data.node;
            $scope.sessionid = data.sessionId;

        });
    }

    $scope.deleteAttribute = function(attr) {
        SessionAttribute.delete(attr, function(data) {
            $scope.allAttributes = data.attributes;
            $scope.node = data.node;
            $scope.sessionid = data.sessionId;
        });

    }

    $scope.deleteAttribute2 = function(attr) {
        attr.$delete(function() {
            $scope.getAttributes();
        });

    }


    $scope.getAttributes();

    



}])




;