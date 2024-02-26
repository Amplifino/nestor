function historyCtrl($scope, HttpService) {

  this.$onInit = function() {
    $scope.ui = this.ui;
  }

  $scope.$watch('$scope.ui.activeDS', function() {
    if ($scope.ui.activeDS) $scope.getHistory();
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
