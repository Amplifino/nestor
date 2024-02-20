class Field {
  constructor(table, field) {
    this.table = {};
    this.table.name = table.name;
    this.table.alias = table.alias;
    this.name = field.name;
    this.alias = table.alias + '.' + field.name.toLowerCase();
  }
}
