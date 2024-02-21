function navigationCtrl($scope) {
  this.$onInit = function() { $scope.ui = this.ui; }
}

app.component('navigation', {
  templateUrl: './components/navigation.html',
  controller: navigationCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

const TabId = {
  SQL: 'SQL',
  RESULT: 'RESULT',
  ERRORS: 'ERRORS',
  UTILS: 'UTILS',
}

class Tab {
  constructor(tabId, display) {
    this.tabId = tabId;
    this.display = display;
    this.count = 0;
    this.active = false;
  }
}

class Navigation {

  constructor() {
    this.tabs = new Array(); // Array<Tab>();
    const sqlTab = new Tab(TabId.SQL, 'SQL');
    sqlTab.active = true;
    this.activeTab = sqlTab;
    this.tabs.push(sqlTab);
    this.tabs.push(new Tab(TabId.RESULT, 'Results'));
    this.tabs.push(new Tab(TabId.ERRORS, 'Errors'));
    this.tabs.push(new Tab(TabId.UTILS, 'Utilities'));
  }

  selectTab(tabId, count) {
    var found = null;
    for (var idx = 0; idx < this.tabs.length; idx++) {
      const tb = this.tabs[idx];
      tb.active = false;
      if (tb.tabId === tabId) found = tb;
    }
    const useTab = found ? found : this.tabs[0];
    useTab.active = true;
    useTab.count = count || 0;
    this.activeTab = useTab;
  }

  open(element) {
    const instance = M.Dropdown.getInstance(element);
    instance.open();
  }

}
