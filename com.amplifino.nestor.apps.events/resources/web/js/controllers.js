var controllers = angular.module('eventControllers', []);

controllers.controller('EventCtrl', ['$scope', 
		function ($scope) {
	$scope.events = [];
	$scope.source = new EventSource("/api/events");
	$scope.source.onopen = function() {
		console.log("open");
	}
	$scope.source.onmessage = function(event) {
		console.log("received:" + event);
		$scope.events.unshift(JSON.parse(event.data));
		$scope.$apply();
	};
}]);
