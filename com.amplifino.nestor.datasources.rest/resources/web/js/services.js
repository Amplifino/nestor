var services = angular.module('Services', ['ngResource']);

services.service('HttpService', ['$resource',
  function($resource) {
    this.getDataSources = function() {
      return $resource('/api/datasources').query().$promise;
    }
    this.getTables = function(ds) {
      const url = '/api/datasources/' + ds.name + '/tables';
      return $resource(url).query().$promise;
    }
    this.getTableFields = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/columns';
      return $resource(url).query().$promise;
    }
    this.runQuery = function(ds, sql) {
      const url = '/api/datasources/' + ds.name;
      return $resource(url).save(sql).$promise;
    }
}]);
