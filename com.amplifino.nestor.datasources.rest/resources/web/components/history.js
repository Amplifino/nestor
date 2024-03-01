function historyCtrl($scope, HttpService) {

  this.$onInit = function() {
    $scope.ui = this.ui;
  }

  $scope.$watch('$scope.ui.activeDS', function() {
    if ($scope.ui.activeDS) {
      $scope.ui.enableHistory = true;
      $scope.showCreateTable = false;
      $scope.ui.history.length = 0;
      $scope.getHistory();
    }
  });

  $scope.$watch('$scope.ui.sql', function() {
    const cleanSql = $scope.ui.getCleanSql().toUpperCase();
    for (var idx = 0; idx < $scope.ui.history.length; idx++) {
      const history = $scope.ui.history[idx];
      const sql = history.query;
      history.show = (sql.length > 0 && sql.toUpperCase().startsWith(cleanSql));
    }
  });

  $scope.showCreateTable = false;

  $scope.getHistory = function() {
    HttpService
      .getHistory($scope.ui.activeDS)
      .then(response => { $scope.ui.setHistory(response); })
      .catch(err => {
        $scope.ui.enableHistory = false;
        $scope.showCreateTable = true;
        console.error('historyCtrl.getHistory(): ' + JSON.stringify(err));
      });
  }

  $scope.closeMessage = function() {
    $scope.showCreateTable = false;
  }

}

app.component('history', {
  templateUrl: './components/history.html',
  controller: historyCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});
