function queriesCtrl($scope, HttpService) {
  this.$onInit = function() { $scope.ui = this.ui; }
  $scope.runQuery = function() {
    HttpService
      .runQuery($scope.ui.activeDS, $scope.ui.sql)
        .then(response => { $scope.ui.setResult(response); })
        .catch(err => console.error('queriesCtrl.runQuery(): ' + JSON.stringify(err)));
  }
}

app.component('queries', {
  templateUrl: './components/queries.html',
  controller: queriesCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});
