var userAdminApp = angular.module('userAdminApp', [
  'ngRoute',
  'userAdminControllers',                                       	
  'userAdminServices'
]);

userAdminApp.config(['$routeProvider', function ($routeProvider) {
	$routeProvider.when('/userList', {templateUrl: 'partials/userList.html', controller: 'UserListCtrl'});
	$routeProvider.when('/userEdit/:name' , {templateUrl: 'partials/userEdit.html', controller: 'UserEditCtrl'});
	$routeProvider.when('/userChangePassword/:name' , {templateUrl: 'partials/userPass.html', controller: 'UserPassCtrl'});
    $routeProvider.when('/userCreation', {templateUrl: 'partials/userCreation.html', controller: 'UserEditCtrl'});
    $routeProvider.when('/groupList', {templateUrl: 'partials/groupList.html', controller: 'GroupListCtrl'});
    $routeProvider.when('/groupEdit/:name' , {templateUrl: 'partials/groupEdit.html', controller: 'GroupEditCtrl'});
    $routeProvider.when('/groupCreation', {templateUrl: 'partials/groupCreation.html', controller: 'GroupEditCtrl'});
}]);
