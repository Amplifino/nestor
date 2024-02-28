function queriesCtrl($scope, HttpService) {

  this.$onInit = function() {
    $scope.ui = this.ui;
  }

  $scope.runQuery = function() {
    HttpService
      .runQuery($scope.ui.activeDS, $scope.ui.sql)
      .then(response => { $scope.ui.setResult(response); })
      .catch(err => console.error('queriesCtrl.runQuery(): ' + JSON.stringify(err)));
  }

  $scope.$watch('$scope.ui.sql', function() {
    updateSegments($scope.ui);
    document.getElementById('sqlInput').focus();
  });
}

app.component('queries', {
  templateUrl: './components/queries.html',
  controller: queriesCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

updateSegments = function(ui) {
  for (const [key, statement] of Object.entries(ui.statements)) {
    statement.resetUI(ui.sql, ui.statements, ui.tables);
  }
  ui.tables.forEach((table) => { table.resetUI(ui.sql, ui.statements, ui.tables); });
}
