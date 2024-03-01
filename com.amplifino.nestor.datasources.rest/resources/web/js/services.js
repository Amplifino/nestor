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
    this.getHistory = function(ds) {
      const url = '/api/datasources/' + ds.name + '/history';
      return $resource(url).query().$promise;
    }
    this.runQuery = function(ds, sql, limit) {
      var url = '/api/datasources/' + ds.name;
      if (limit) url += '?limit=' + limit;
      return $resource(url).save(sql).$promise;
    }
    this.describe = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/describe';
      return $resource(url, {}, { describe: { isArray: false }}).describe().$promise;
    }
    this.keys = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/keys';
      return $resource(url, {}, { keys: { isArray: false }}).keys().$promise;
    }
    this.relations = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/relations';
      return $resource(url, {}, { relations: { isArray: false }}).relations().$promise;
    }
    this.references = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/references';
      return $resource(url, {}, { references: { isArray: false }}).references().$promise;
    }
}]);
