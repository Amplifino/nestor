function resultCtrl($scope) {
  this.$onInit = function() {
    $scope.ui = this.ui;
  }
}

app.component('result', {
  templateUrl: './components/result.html',
  controller: resultCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

class Result {

  constructor(o) {
    this.rowCount = o && o.rowCount ? o.rowCount : 0;
    this.columns = o && o.columns ? o.columns : {};
    this.tuples = o && o.tuples ? o.tuples : new Array();
  }

  getData(row, col) {
    const cTable = col.table;
    const rTable = row[cTable];
    if (!rTable) return '';
    const cName = col.name;
    const rCol = rTable[cName];
    return rCol;
  }

}
