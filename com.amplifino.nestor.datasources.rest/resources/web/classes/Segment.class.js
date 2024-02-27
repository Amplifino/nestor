
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
  buildNgClass() {
    this.ngClass += 'autocomplete-' + this.autocomplete + ' ';
    this.ngClass += 'highlight-' + this.highlight + ' ';
    this.ngClass += 'disabled-' + this.disabled + ' ';
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
  resetUI(sql, statements, tables) {
    super.resetUI(sql);
    if (sqlIsEmpty(sql)) {
      this.disabled = true;
    } else {
      const parts = findLastSqlSegmentParts(sql, statements, tables);
      // console.warn('TableSegment parts: '+JSON.stringify(parts))
      if (!parts.last) {
        this.disabled = true;
      } else {
        if (parts.last.nextType !== Type.TABLE) {
          this.disabled = true;
        } else {
          if (!parts.secondLast) {
            this.disabled = true;
          } else {
            this.highlight = this.name.toUpperCase().startsWith(parts.part);
          }
        }
      }
    }
    this.buildNgClass();
    this.fields.forEach((field) => { field.resetUI(sql, statements, tables); });
    return;
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
  resetUI(sql, statements, tables) {
    super.resetUI(sql);
    if (sqlIsEmpty(sql)) {
      this.disabled = true;
    } else {
      const parts = findLastSqlSegmentParts(sql, statements, tables);
      // console.warn('FieldSegment parts: '+JSON.stringify(parts))
      if (!parts.last && !parts.secondLast) {
        this.disabled = true;
      } else {
        if (parts.last) {
          if (parts.last.nextType !== Type.FIELD) {
            this.disabled = true;
          } else {
            this.highlight = this.name === '*';
            // if (!parts.secondLast) {
            //   this.highlight = this.name === '*';
            // } else {
            //   this.highlight = this.name.toUpperCase().endsWith(parts.part);
            // }
          }
        } else if (parts.secondLast) {
          console.warn('FieldSegment parts: TODO')
        }
      }
    }
    this.buildNgClass();
    return;
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
  resetUI(sql, statements, tables) {
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
    } else {
      const parts = findLastSqlSegmentParts(sql, statements, tables);
      console.warn('StatementSegment parts: '+JSON.stringify(parts))
      if (!parts.last) {
        this.highlight = this.name.toUpperCase().startsWith(parts.part);
      } else {
        if (parts.last.nextType !== Type.STATEMENT) {
          this.disabled = true;
        }
      }
    }
    this.buildNgClass();
    return;
  }

}

function sqlIsEmpty(sql) {
  return !sql || sql === '';
}

function findLastSqlSegmentParts(sql, statements, tables) {
  const s = sql.replaceAll('\n', ' '); // new line > space
  const splitted = s.split(' ');
  const parts = new Array();
  for (var idx = 0; idx < splitted.length; idx++) {
    const check = splitted[idx];
    if (check.length) parts.push(check);
  }
  const part = parts.pop().toUpperCase();
  var secondLastPart = null;
  var secondLastSegmentPart = null;
  if (parts.length) {
    secondLastPart = parts.pop().toUpperCase();
    const statementSecondSegmentPart = findStatement(secondLastPart, statements);
    if (statementSecondSegmentPart.last) {
      secondLastSegmentPart = statementSecondSegmentPart;
    } else {
      secondLastSegmentPart = findTableOrField(secondLastPart, tables);
    }
  }
  const statementSegmentPart = findStatement(part, statements);
  statementSegmentPart.secondLast = secondLastSegmentPart;
  if (statementSegmentPart.last) return statementSegmentPart;
  //
  const tableSegmentPart =  findTableOrField(part, tables);
  tableSegmentPart.secondLast = secondLastSegmentPart;
  return tableSegmentPart;
}

class SegmentPart {
  constructor(part) {
    this.part = part;
    this.last = null;
    this.secondLast = null;
  }
}

function findStatement(part, statements) {
  const segmentPart = new SegmentPart(part);
  const pLength = part.length;
  if (pLength === 0) return segmentPart;
  for (const [key, statement] of Object.entries(statements)) {
    if (key.toUpperCase() === part) {
      segmentPart.last = statement;
      break;
    }
  }
  return segmentPart;
}

function findTableOrField(part, tables) {
  const segmentPart = new SegmentPart(part);
  const pLength = part.length;
  const partNoComma = part.replaceAll(',', '');
  if (pLength === 0) return segmentPart;
  for (var tdx = 0; tdx < tables.length; tdx++){
    const table = tables[tdx];
    if (table.name.toUpperCase() === part) {
      segmentPart.last = table;
      return segmentPart;
    } else {
      for (var fdx = 0; fdx < table.fields.length; fdx++) {
        const field = table.fields[fdx];
        if (field.alias.toUpperCase() === partNoComma) {
          segmentPart.last = field;
          return segmentPart;
        }
      }
    }
  }
  return segmentPart;
}
