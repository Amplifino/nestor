var services = angular.module('Services', ['ngResource']);

services.service('HttpService', ['$resource',
  function($resource) {
    this.getDataSources = function() {
      return $resource('/api/datasources').query().$promise;
    }
    this.getTables = function(dsName) {
      const url = '/api/datasources/' + dsName + '/tables';
      return $resource(url).query().$promise;
    }
    this.getTableFields = function(table) {
      const url = '/api/datasources/' + table.dataSource + '/tables/' + table.name + '/columns';
      return $resource(url).query().$promise;
    }
}]);

//services.factory('Column', ['$resource',
//  function($resource) {
//	return $resource('/api/datasources/:dataSource/tables/:name/columns');
//  }]);

//services.factory('Table' , ['$resource', 'Column' ,
//  function($resource, Column) {
//	var constructor = $resource('/api/datasources/:name/tables' , {name: '@name'});
//	constructor.prototype.getColumns = function() {
//		if (!this.columns) {
//			this.columns = Column.query(this);
//		}
//		return this.columns;
//	}
//	return constructor;
//  }]);

// services.factory('DataSource', ['$resource', 'Table' ,
//   function($resource, Table) {
// 	var constructor = $resource('/api/datasources/:name' , {} , {
// 		'select' : { method: 'POST' }});
// 	constructor.prototype.getTables = function() {
// 		if (!this.tables) {
// 			this.tables = Table.query(this);
// 		}
// 		return this.tables;
// 	};
// 	return constructor;
//   }]);
