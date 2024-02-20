function queriesCtrl($scope) {
  this.$onInit = function() { $scope.ui = this.ui; }
}

app.component('queries', {
  templateUrl: './components/queries.html',
  controller: queriesCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});
