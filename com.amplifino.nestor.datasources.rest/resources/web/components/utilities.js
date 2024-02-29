function utilitiesCtrl($scope, HttpService) {

  this.$onInit = function() {
    $scope.ui = this.ui;
  }

  $scope.showResult = false;
  $scope.result = new Array();
  $scope.activeTable = null;
  $scope.utilities = initUtilities();

  $scope.setTable = function(table) {
    $scope.activeTable = table;
  }

  $scope.openResultView = function(utility) {
    $scope.showResult = utility;
    $scope.result.length = 0;
    var promise = null;
    switch (utility) {
      case 'describe':    promise = HttpService.describe($scope.activeTable); break;
      case 'keys':        promise = HttpService.keys($scope.activeTable); break;
      case 'relations':   promise = HttpService.relations($scope.activeTable); break;
      case 'references':  promise = HttpService.references($scope.activeTable); break;
      case 'create statement': break;
      case 'update statement': break;
      case 'insert statement': break;
      default: break;
    }
    if (promise) {
      promise
        .then(response => { console.warn('response: '+JSON.stringify(response)); $scope.result = response.tuples;  })
        .catch(err => {
          $scope.result = [ JSON.stringify(err) ];debugger;
          console.error('utilitiesCtrl.openResultView(): ' + JSON.stringify(err));
        });
    }
  }

}

app.component('utilities', {
  templateUrl: './components/utilities.html',
  controller: utilitiesCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

function initUtilities() {
  const arr = new Array();
  arr.push('describe');
  arr.push('keys');
  arr.push('relations');
  arr.push('references');
  arr.push('create statement');
  arr.push('update statement');
  arr.push('insert statement');
  return arr;
}
