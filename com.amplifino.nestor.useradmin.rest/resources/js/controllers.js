var userAdminControllers = angular.module('userAdminControllers', []);

userAdminControllers.controller('UserListCtrl', ['$scope', 'User', '$location' , function ($scope, User, $location) {
	
  $scope.createNewUser = function() {
	  $location.path('/userCreation');
  };
  
  $scope.editUser = function(user) {
	  $location.path('/userEdit/' + user.name);
  };
  
  $scope.deleteUser = function(user) {
	  user.$delete();
	  $scope.users = User.query();  	  
  };
  
  $scope.changePassword = function(user) {
	  $location.path('userChangePassword/' + user.name);
  }
  
  $scope.users = User.query();
  $scope.users.$promise.then(null, function(err) {
	  $scope.status = "Error: " + err.status + ": " + err.statusText;	  
  });
}]);

userAdminControllers.controller('UserEditCtrl', ['$scope', '$routeParams' , 'User', '$location', function ($scope, $routeParams, User, $location) {
	   $scope.status = "";
	   $scope.newProp = { key: "" , value: "", isValid: function() {
		   return this.key && this.value;
	   }};
	   if ($routeParams.name) {
		   $scope.user = User.get({name: $routeParams.name});
	   } else {
		   $scope.user = new User();
		   $scope.user.properties = {};
	   }
	   
	   $scope.createNewUser = function () {
			$scope.user.$save(function() { 
				$location.path('/userList');
			}, function() { 
				$scope.status = "Failed to create user " + $scope.user.name;
			});
	   };
	   
	   $scope.updateUser = function () {
			$scope.user.$update(function() { 
				$location.path('/userList');
			}, function() { 
				$scope.status = "Failed to update user " + $scope.user.name;
			});
	   };
	   
	   $scope.cancelEditUser = function() {
			$location.path('/userList');
	   }
	}]);

userAdminControllers.controller('UserDetailCtrl', ['$scope',  function ($scope) {
	   
	   $scope.addProperty = function() {
		   $scope.user.setProperty($scope.key,$scope.value);
		   $scope.key = "";
		   $scope.value= "";
	   };
	   	   	   
 	}]);

userAdminControllers.controller('GroupListCtrl', ['$scope', 'Group', '$location' , function ($scope, Group, $location) {
	  $scope.createNewGroup = function() {
		  $location.path('/groupCreation');
	  };
	  
	  $scope.editGroup = function(group) {
		  $location.path('/groupEdit/' + group.name);
	  };
	  
	  $scope.deleteGroup = function(group) {
		  group.$delete();
		  $scope.groups = Group.query();  	  
	  };
	  
	  $scope.groups = Group.query();
	}]);

userAdminControllers.controller('GroupEditCtrl', ['$scope', '$routeParams' , 'Group', 'Role', '$location', function ($scope, $routeParams, Group, Role, $location) {

	$scope.status = "";
	$scope.roles = Role.query();
	
	if ($routeParams.name) {
	   $scope.group = Group.get({name: $routeParams.name});
	} else {
	   $scope.group = new Group();		  
	}
	   
	$scope.createNewGroup = function () {
		$scope.group.$save(function() { 
			$location.path('/groupList');
		}, function() {
			$scope.status = "Failed to create group " + $scope.group.name;
		});
	};
	   
	$scope.updateGroup = function () {
		$scope.group.$update(function() { 
			$location.path('/groupList');
		}, function() { 
			$scope.status = "Failed to update group " + $scope.group.name;
		});
	};
	
	$scope.canCreate = function() {
		if (!$scope.group.name) {
		   return false;
		}
		return !$scope.roles.some(function(role) {
	   		return role.name == $scope.group.name;
	  	});		   
	};	   
}]);

userAdminControllers.controller('GroupDetailCtrl', ['$scope', function ($scope) {

	   $scope.addProperty = function() {
		   $scope.group.setProperty($scope.key, $scope.value);
		   $scope.key = "";
		   $scope.value = "";
	   };
	   	   
	   
	   $scope.canAddMember = function(role) {
		   return $scope.group.canAddMember(role);
	   };
	   
	   $scope.url = function(memberOrRole) {
			  if (memberOrRole.type == 'User') {
				  return '#/userEdit/' + memberOrRole.name; 
			  }
			  if (memberOrRole.type == 'Group') {
				  return '#/groupEdit/' + memberOrRole.name; 
			  }
			  return '';
	   };
	}]);

userAdminControllers.controller('UserPassCtrl', ['$scope', '$routeParams' , '$http', '$location', function ($scope, $routeParams, $http, $location) {
	   $scope.status = "";
	   $scope.userName = $routeParams.name;
	   
	   $scope.updatePassword = function (newPass, checkPass) {
		   if (newPass && (newPass == checkPass)) {
			   $http.post("/api/useradmin/users/" + $scope.userName + "/ha1", newPass)
		    		.then( function() { 
		    			$location.path('/userList');
		    			}, function() { 
		    				$scope.status = "Failed to update user " + $scope.userName;
		    			});
		   	} else {
		   		$scope.status = "passes are different";
		   	}
	   };
	   
	}]);
