
const Type = {
  STATEMENT: 'STATEMENT',
  TABLE:     'TABLE',
  FIELD:     'FIELD',
  ALIAS:     'ALIAS',
  NUMBER:    'NUMBER',
}

class AbstractSegment {
  constructor(o, type, nextType) {
    this.name = o && o.name ? o.name : 'noName';
    this.type = type;
    this.nextType = nextType;
    this.disabled = false;
    this.highlight = false;
    this.autocomplete = false;
    this.ngClass = 'type-' + this.type + ' ';
  }
  resetUI(sql) {
    this.disabled = false;
    this.highlight = false;
    this.autocomplete = false;
    this.ngClass = '';
  }
  insert() {
    return ' ' + this.name.toUpperCase() + ' ';
  }
}

/*
 * TABLE
 */
class TableSegment extends AbstractSegment {

  constructor(o, httpService) {
    super(o, Type.TABLE, Type.STATEMENT);
    this.alias = 'notInitialised';
    this.dataSource = o && o.dataSource ? o.dataSource : 'noDataSource';
    this.fields = new Array(); // Array<FieldSegment>()
    initTableFields(this, httpService);
  }

  /** @override */
  insert() {
    return ' ' + this.name.toUpperCase() + ' as ' + this.alias.toLowerCase() + ' ';
  }

}

function initTableFields(table, httpService) {
  httpService
    .getTableFields(table)
      .then(response => { setTableFields(table, response); })
      .catch(err => console.error('getTableFields(): ' + err));
}

function setTableFields(table, response) {
  table.fields.push(new FieldSegment(table, { name: '*'}));
  response.forEach((entry) => {
    const field = new FieldSegment(table, entry);
    table.fields.push(field);
  });

}

/*
 * FIELD
 */
class FieldSegment extends AbstractSegment {
  constructor(table, field) {
    super(field, Type.FIELD, Type.STATEMENT);
    this.table = {};
    this.table.name = table.name;
    this.table.alias = table.alias;
    this.alias = table.alias + '.' + field.name.toLowerCase();
  }
  /** @override */
  insert() {
    return ' ' + this.alias + ', ';
  }
}

/*
 * STATEMENT
 */
class StatementSegment extends AbstractSegment {

  constructor(name, nextType) {
    super({ name: name }, Type.STATEMENT, nextType);
  }

  /** @override */
  resetUI(sql) {
    super.resetUI(sql);
    if (sqlIsEmpty(sql)) {
      switch (this.name) {
        case 'SELECT':
          this.highlight = true;
          this.autocomplete = true;
          break;
        case 'UPDATE':
        case 'DELETE': break;
        default:
          this.disabled = true;
          break;
      }
    }
    this.ngClass += 'autocomplete-' + this.autocomplete + ' ';
    this.ngClass += 'highlight-' + this.highlight + ' ';
    this.ngClass += 'disabled-' + this.disabled + ' ';
  }

}

function sqlIsEmpty(sql) {
  return !sql || sql === '';
}
