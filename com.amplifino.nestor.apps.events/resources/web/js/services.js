var services = angular.module('eventServices', ['ngResource']);

services.factory('Event', ['$resource',
  function($resource) {
	return $resource('/api/events')
  }]);

                                
                                                                        