class Ui {

  constructor() {
    this.dataSources = new Array(); // Array<String>()
    this.activeDS = null;
    this.tables = new Array(); // Array<Table>()
    this.activeTable = null;
    this.nav = new Navigation();
    this.sql = 'SELECT * from project p join time_registration t on t.project_id = p.id limit 500';
    this.result = new Result();
    this.statements = Statement;
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

const Type = {
  STATEMENT: 'STATEMENT',
  TABLE: 'TABLE',
  FIELD: 'FIELD',
  ALIAS: 'ALIAS',
}

const Statement = {
  SELECT: { text: 'SELECT', type: Type.STATEMENT, sort: 1, nextType: Type.FIELD },
  UPDATE: { text: 'UPDATE', type: Type.STATEMENT, sort: 1, nextType: Type.TABLE },
  DELETE: { text: 'DELETE', type: Type.STATEMENT, sort: 1, nextType: Type.STATEMENT },
  JOIN: { text: 'JOIN', type: Type.STATEMENT, sort: 1, nextType: Type.TABLE },
  INNER_JOIN: { text: 'INNER JOIN', type: Type.STATEMENT, sort: 1, nextType: Type.TABLE },
  OUTER_JOIN: { text: 'OUTER JOIN', type: Type.STATEMENT, sort: 1, nextType: Type.TABLE },
  FROM: { text: 'FROM', type: Type.STATEMENT, sort: 1, nextType: Type.TABLE },
  AS: { text: 'AS', type: Type.STATEMENT, sort: 1, nextType: Type.ALIAS },
  WHERE: { text: 'WHERE', type: Type.STATEMENT, sort: 1, nextType: Type.FIELD },
  ORDER_BY: { text: 'ORDER BY', type: Type.STATEMENT, sort: 1, nextType: Type.FIELD },
}
