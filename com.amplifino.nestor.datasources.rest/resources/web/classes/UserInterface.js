class Ui {

  constructor() {
    this.dataSources = new Array(); // Array<String>()
    this.activeDS = null;
    this.tables = new Array(); // Array<TableSegment>()
    this.activeTable = null;
    this.nav = new Navigation();
    this.sql = 'SELECT * from project p join time_registration t on t.project_id = p.id limit 500';
    this.result = new Result();
    this.statements = initStatements();
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

  addSqlInput(segment) {
    const el = document.getElementById('sqlInput');
    const cursorPosition = el.selectionStart || this.sql.length;
    const preCursor = this.sql.substring(0, cursorPosition);
    const postCursor = this.sql.substring(cursorPosition, this.sql.length);
    const text = segment.insert();
    this.sql = preCursor + ' ' + text + ' ' + postCursor;
    el.focus();
    el.selectionStart = preCursor.length + text.length;
  }

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
  o.UPDATE = new StatementSegment('UPDATE', Type.TABLE);
  o.DELETE = new StatementSegment('DELETE', Type.STATEMENT);
  o.JOIN = new StatementSegment('JOIN', Type.TABLE);
  o.INNER_JOIN = new StatementSegment('INNER JOIN', Type.TABLE);
  o.OUTER_JOIN = new StatementSegment('OUTER JOIN', Type.TABLE);
  o.FROM = new StatementSegment('FROM', Type.TABLE);
  o.AS = new StatementSegment('AS', Type.ALIAS);
  o.WHERE = new StatementSegment('WHERE', Type.FIELD);
  o.ORDER_BY = new StatementSegment('ORDER BY', Type.FIELD);
  o.LIMIT = new StatementSegment('LIMIT', Type.NUMBER);
  return o;
}
