function navigationCtrl($scope) {
  this.$onInit = function() { $scope.ui = this.ui; }
}

app.component('navigation', {
  templateUrl: './components/navigation.html',
  controller: navigationCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

class Tab {
  constructor(display) {
    this.display = display;
    this.count = 0;
    this.active = false;
  }
}

class Navigation {

  constructor() {
    this.tabs = new Array(); // Array<Tab>();
    const sqlTab = new Tab('SQL');
    sqlTab.active = true;
    this.activeTab = sqlTab;
    this.tabs.push(sqlTab);
    this.tabs.push(new Tab('Results'));
    this.tabs.push(new Tab('Errors'));
  }

  selectTab(tab) {
    for (var idx = 0; idx < this.tabs.length; idx++) {
      const tb = this.tabs[idx];
      tb.active = false;
    }
    tab.active = true;
    this.activeTab = tab;
  }

  open(element) {
    const instance = M.Dropdown.getInstance(element);
    instance.open();
  }

}
