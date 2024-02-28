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
  }

  selectDS(ds) {
    this.activeDS = ds;
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

  logError(msg, stringify) {
    const json = stringify ? ': ' + JSON.stringify(stringify) : '';
    console.error(msg + json);
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
