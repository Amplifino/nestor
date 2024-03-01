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
          $scope.ui.initTables(HttpService);
          //
          // $scope.$watch('$scope.ui.activeDS', function() {
          //   console.warn('MainCtrl');
          //   if ($scope.ui.activeDS) initTables($scope.ui, HttpService);
          // });
        }
    ]
);
