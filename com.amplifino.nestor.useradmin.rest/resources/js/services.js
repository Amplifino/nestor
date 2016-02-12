var userAdminServices = angular.module('userAdminServices', ['ngResource']);

userAdminServices.factory('User', ['$resource',
  function($resource) {
    var result = $resource('/api/useradmin/users/:name', {}, {
      'delete': { method: 'DELETE' , params: { name: '@name' }},
      update: { method: 'PUT' , params: {name: '@name'}}
    });
    
    result.prototype.setProperty = function (key,value) {
    	if (!this.properties) {
    		this.properties = {};
    	}
    	this.properties[key] = value;
    };
    
    result.prototype.removeProperty = function(key) {
		   delete this.properties[key];
	   };
	
    result.prototype.hasProperty = function(key) {
    	if (!key) {
    		return false;
		}
		if (this.properties) {
			return this.properties.hasOwnProperty(key);
		} else {
	  	  	return false;
	   	}
	};
	
	result.prototype.getPropertyNames = function() {
		if (this.properties) {
			return Object.getOwnPropertyNames(this.properties);
		} else {
			return [];
		}
	};
	
    return result;
 }]);

userAdminServices.factory('Group', ['$resource',
  function($resource) {
	var result = $resource('/api/useradmin/groups/:name', {}, {
		'delete': { method: 'DELETE' , params: { name: '@name' }},
		update: { method: 'PUT' , params: {name: '@name'}}
	});
	
	result.prototype.removeProperty = function(key) {
		   delete this.properties[key];
	   };
	   
	result.prototype.setProperty = function(key,value) {
		if (!this.properties) {
			this.properties = {};
		}
		this.properties[key] = value;
	};
	
	result.prototype.hasProperty = function(key) {
    	if (!key) {
    		return false;
		}
		if (this.properties) {
			return this.properties.hasOwnProperty(key);
		} else {
	  	  	return false;
	   	}
	};
	
	result.prototype.getPropertyNames = function() {
		if (this.properties) {
			return Object.getOwnPropertyNames(this.properties);
		} else {
			return [];
		}
	};
	   
	result.prototype.canAddMember = function(role) {		
		if (role.name == this.name) {
		   return false;
		}
		if (this.members) {
		   return !this.members.some( function(member) { 
			   return member.name == role.name; 
		   });
	   } else {
		   return true;
	   }
	};
	   
	result.prototype.addMember = function(role) {
		if (!this.members) {
			this.members = [];
		}
		this.members.push({ name: role.name , type: role.type, required: false});
    };
	   
	result.prototype.removeMember = function(member) {
		this.members.splice(this.members.indexOf(member), 1);
	};
	
	return result;
}]);

userAdminServices.factory('Role', ['$resource',
   function($resource) {
	  return $resource('/api/useradmin/roles', {}, {});
}]);

