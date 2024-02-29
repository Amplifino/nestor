function errorsCtrl($scope, HttpService) {
  this.$onInit = function() {
    $scope.ui = this.ui;
  }
}

app.component('errors', {
  templateUrl: './components/errors.html',
  controller: errorsCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

class SqlError {
  constructor(o) {
    this.sql = o && o.sql ? o.sql : 'no-sql';
    this.cause = o && o.cause ? o.cause : 'no-cause';
    this.message = o && o.message ? o.message : 'no-message';
    this.state = o && o.state ? o.state : 'no-state';
  }
}
