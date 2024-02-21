function resultCtrl($scope) {
  this.$onInit = function() { $scope.ui = this.ui; }
}

app.component('result', {
  templateUrl: './components/result.html',
  controller: resultCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

class Result {

  constructor(o) {
    rowCount: o && o.rowCount ? o.rowCount : 0;
    tuples: o && o.tuples ? o.tuples : new Array();
  }

}
