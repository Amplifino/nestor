var dataSourceServices = angular.module('sqlServices', ['ngResource']);

dataSourceServices.factory('Column', ['$resource',
  function($resource) {
	return $resource('/api/datasources/:dataSource/tables/:name/columns');
  }]);

dataSourceServices.factory('Table' , ['$resource', 'Column' ,
  function($resource, Column) {
	var constructor = $resource('/api/datasources/:name/tables' , {name: '@name'});
	constructor.prototype.getColumns = function() {
		if (!this.columns) {
			this.columns = Column.query(this);
		}
		return this.columns;
	}
	return constructor;
  }]);

dataSourceServices.factory('DataSource', ['$resource', 'Table' , 
  function($resource, Table) {
	var constructor = $resource('/api/datasources/:name' , {} , {
		'select' : { method: 'POST' }});
	constructor.prototype.getTables = function() {
		if (!this.tables) {
			this.tables = Table.query(this);
		}
		return this.tables;
	};
	return constructor;
  }]);
                                
                                                                        