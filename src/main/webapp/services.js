'use strict';

angular.module('ClusteringTestApp.services', ['ngResource'])

.factory('Notifications', function($rootScope, $timeout) {
	// time (in ms) the notifications are shown
	var delay = 5000;

	var notifications = {};
    notifications.current = { display: false };
    notifications.current.remove = function() {
        if (notifications.scheduled) {
            $timeout.cancel(notifications.scheduled);
            delete notifications.scheduled;
        }
        delete notifications.current.type;
        delete notifications.current.header;
        delete notifications.current.message;
        notifications.current.display = false;
        console.debug("Remove message");
    }

    $rootScope.notification = notifications.current;

	notifications.message = function(type, header, message) {
        notifications.current.remove();

        notifications.current.type = type;
        notifications.current.header = header;
        notifications.current.message = message;
        notifications.current.display = true;

        notifications.scheduled = $timeout(function() {
            notifications.current.remove();
        }, delay);

        console.debug("Added message");
	}

	notifications.info = function(message) {
		notifications.message("info", "Info!", message);
	};

	notifications.success = function(message) {
		notifications.message("success", "Success!", message);
	};

	notifications.error = function(message) {
		notifications.message("danger", "Error!", message);
	};

	notifications.warn = function(message) {
		notifications.message("warning", "Warning!", message);
	};

	return notifications;
})

.factory('OpenshiftService', ['$resource', function($resource) {
    return {
        login: $resource('/rest/openshift/authenticate/:token', {token:'@token'}, {
            put: {
                method: 'PUT'
            }
        }),
        pods: $resource('/rest/openshift/:project/:service/pods/:name', {}, {
            delete: {
                method: 'DELETE',
                params: { name: '@name'},
                isArray: false
            }
        })
    };
}])





.factory('SessionAttribute', ['$resource', function($resource) {
    //return $resource('/rest/session/attributes/:key'); 
    return $resource('/rest/session/attributes/:key', {}, {
        query: {
            method: 'GET',
            isArray: false,
        },
        delete: {
            method: 'DELETE',
            params: { key: '@key'},
            headers: {'Content-Type': 'application/json'},
        }
    })
}])


;