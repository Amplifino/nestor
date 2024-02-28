var app = angular.module('adHocSql2', [
    'Controllers',
    'Services'
]);

var controllers = angular.module('Controllers', []);

controllers.controller('MainCtrl',
    [
        '$scope', 'HttpService',
        function ($scope, HttpService) {
          $scope.ui = new Ui();
          initDataSource($scope.ui, HttpService);
          document.onkeydown = function(event) { $scope.ui.handleKeydown($scope, event); };

          $scope.$watch('$scope.ui.activeDS', function() {
            if ($scope.ui.activeDS) initTables($scope.ui, HttpService);
          });
        }
    ]
);

function initMaterializeCss() {
  // https://materializecss.com/dropdown.html
  const dropdownElements = document.querySelectorAll('.dropdown-trigger');
  const dropdownOptions = {};
  const dropdownInstances = M.Dropdown.init(dropdownElements, dropdownOptions);
}

function initDataSource(ui, httpService) {
  ui.dataSources.length = 0;
  ui.tables.length = 0;
	httpService
    .getDataSources()
      .then(response => { setDataSources(ui, response, httpService); })
      .catch(err => console.error('getDataSources(): ' + err));
}

function setDataSources(ui, response, httpService) {
  ui.dataSources = response;
  ui.activeDS = null;
  ui.tables.length = 0;
	if (ui.dataSources.length > 0) {
		ui.activeDS = response[0];
    initTables(ui, httpService);
	}
}

function initTables(ui, httpService) {
  httpService
    .getTables(ui.activeDS)
      .then(response => { setTables(ui, response, httpService); })
      .catch(err => console.error('getTables(): ' + err));
}

function setTables(ui, response, httpService) {
  ui.tables.length = 0;
  ui.activeTable = null;
  for (var idx = 0; idx < response.length; idx++) {
    const table = response[idx];
    if (table.name === 'QUERY_HISTORY') continue;
    const segment = new TableSegment(table, httpService);
    ui.tables.push(segment);
  }
  ui.initTableAliases();
  initMaterializeCss();
}
