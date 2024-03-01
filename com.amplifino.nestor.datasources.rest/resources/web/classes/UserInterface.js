class Ui {

  constructor() {
    this.statements = initStatements();
    this.dataSources = new Array(); // Array<String>()
    this.activeDS = null;
    this.tables = new Array(); // Array<TableSegment>()
    this.activeTable = null;
    this.nav = new Navigation();
    this.sql = '';
    this.result = new Result();
    this.enableHistory = true;
    this.history = new Array();
    this.errors = new Array(); // Array<SqlError>()
    this.limitDefault = 500;
    this.limit = this.limitDefault;
  }

  selectDS(ds) {
    this.activeDS = ds;
    this.enableHistory = true;
    this.tables.length = 0;
  }

  initTables(httpService) {
    if (!this.activeDS) return;
    httpService
      .getTables(this.activeDS)
        .then(response => { setTables(this, response, httpService); })
        .catch(err => console.error('getTables(): ' + err));
  }

  initTableAliases() {
    const aliases = {}; // map(key, tableAlias)
    for (var idx = 0; idx < this.tables.length; idx++) {
      const table = this.tables[idx];
      const alias = getAlias(table);
      if (!aliases[alias]) {
        aliases[alias] = alias;

      } else {
        aliases[alias] = table.name.toLowerCase();
      }
      table.alias = aliases[alias];
    }
  }

  getFields() {
    const fields = new Array(); // Array<Field>();
    for (var idx = 0; idx < this.tables.length; idx++) {
      const table = this.tables[idx];
      for (var fdx = 0; fdx < table.fields.length; fdx++) {
        const field = table.fields[fdx];
        fields.push(field);
      }
    }
    return fields;
  }

  setResult(result) {
    this.result.rowCount = result.rowCount;
    this.result.columns = result.columns
    this.result.tuples = result.tuples
    this.nav.selectTab(TabId.RESULT, this.result.rowCount);
  }

  setHistory(response) {
    this.history = response;
  }

  addSqlInput(segment) {
    this.sql = this.sql.replaceAll('  ', ' '); // remove double spaces
    const el = document.getElementById('sqlInput');
    const cursorPosition = el.selectionStart || this.sql.length;
    const preCursor = this.sql.substring(0, cursorPosition);
    const postCursor = this.sql.substring(cursorPosition, this.sql.length);
    const text = segment.insert();
    this.sql = preCursor + ' ' + text + ' ' + postCursor;
    this.sql = this.sql.replaceAll('  ', ' '); // remove double spaces
    el.focus();
    el.selectionStart = preCursor.length + text.length;
  }

  handleKeydown(scope, event) {
    if (!event || event.code !== 'Space') return;
    if (event.isTrusted &&
      event.ctrlKey &&
      !event.altKey && !event.shiftKey && !event.metaKey && !event.repeat) {
        const segment = findAutocompleteStatement(this.statements) || findAutocompleteSegment(this.tables);
        if (segment) {
          this.addSqlInput(segment);
          scope.$apply();
        }
      }
  }

  getCleanSql() {
    const removeEndOfLines = this.sql.replace(/[\n\r]/g, ' ');
    var noDoubleSpaces = removeEndOfLines.replaceAll('  ', ' ');
    var hasDoublespaces = noDoubleSpaces.includes('  ');
    while (hasDoublespaces) {
      noDoubleSpaces = noDoubleSpaces.replaceAll('  ', ' ');
      hasDoublespaces = noDoubleSpaces.includes('  ');
    }
    return noDoubleSpaces;
  }

  logError(internalMsg, stringify) {
    const data = stringify && stringify.data ? stringify.data : {};
    if (data && data.sql && data.cause && data.message) {
      const error = new SqlError(data);
      this.errors.push(error);
      this.nav.selectTab(TabId.ERRORS, this.errors.length);
    } else {
      const json = stringify ? ': ' + JSON.stringify(stringify) : '';
      console.error(internalMsg + json);
    }
  }

}

function findAutocompleteStatement(statements) {
  for (const [key, statement] of Object.entries(statements)) {
    if (statement.autocomplete) return statement;
  }
  return null;
}
function findAutocompleteSegment(tables) {
  for (var tdx = 0; tdx < tables.length; tdx++) {
    const table = tables[tdx];
    if (table.autocomplete) return table;
    for (var fdx = 0; fdx < table.fields.length; fdx++) {
      const field = table.fields[fdx];
      if (field.autocomplete) return field;
    }
  }
  return null;
}

function getAlias(table) {
  const parts = table.name.split('_');
  var alias = '';
  for (var idx = 0; idx < parts.length; idx++) {
    const part = parts[idx];
    if (part.length > 0) {
      const c = part[0];
      alias += c.toLowerCase();
    }
  }
  return alias;
}

function initStatements() {
  const o = {};
  o.SELECT = new StatementSegment('SELECT', Type.FIELD);
  o.FROM = new StatementSegment('FROM', Type.TABLE);
  o.UPDATE = new StatementSegment('UPDATE', Type.TABLE);
  o.SET = new StatementSegment('SET', Type.FIELD);
  o.DELETE = new StatementSegment('DELETE', Type.STATEMENT);
  o.JOIN = new StatementSegment('JOIN', Type.TABLE);
  o.INNER_JOIN = new StatementSegment('INNER JOIN', Type.TABLE);
  o.OUTER_JOIN = new StatementSegment('OUTER JOIN', Type.TABLE);
  o.ON = new StatementSegment('ON', Type.FIELD);

  o.AS = new StatementSegment('AS', Type.ALIAS);
  o.WHERE = new StatementSegment('WHERE', Type.FIELD);
  o.ORDER_BY = new StatementSegment('ORDER BY', Type.FIELD);
  o.LIMIT = new StatementSegment('LIMIT', Type.NUMBER);
  return o;
}

function initMaterializeCss() {
  // https://materializecss.com/dropdown.html
  const dropdownElements = document.querySelectorAll('.dropdown-trigger');
  const dropdownOptions = {};
  const dropdownInstances = M.Dropdown.init(dropdownElements, dropdownOptions);
}

function initDataSource(ui, httpService) {
  ui.dataSources.length = 0;
  ui.tables.length = 0;
	httpService
    .getDataSources()
      .then(response => { setDataSources(ui, response, httpService); })
      .catch(err => console.error('getDataSources(): ' + err));
}

function setDataSources(ui, response, httpService) {
  ui.dataSources = response;
  ui.activeDS = null;
  ui.enableHistory = true;
  ui.tables.length = 0;
	if (ui.dataSources.length > 0) {
		ui.activeDS = response[0];
    ui.initTables(httpService);
	}
}

function setTables(ui, response, httpService) {
  ui.tables.length = 0;
  ui.activeTable = null;
  for (var idx = 0; idx < response.length; idx++) {
    const table = response[idx];
    if (table.name === 'QUERY_HISTORY') continue;
    const segment = new TableSegment(table, httpService);
    ui.tables.push(segment);
  }
  ui.initTableAliases();
  initMaterializeCss();
}
