class Table {

  constructor(o, httpService) {
    this.dataSource = o && o.dataSource ? o.dataSource : 'noDataSource';
    this.name = o && o.name ? o.name : 'noTableName';
    this.fields = new Array(); // Array<String>()
    initTableFields(this, httpService);
  }

}

function initTableFields(table, httpService) {
  httpService
    .getTableFields(table)
      .then(response => { setTableFields(table.fields, response); })
      .catch(err => console.error('getTableFields(): ' + err));
}

function setTableFields(fields, response) {
  response.forEach((field) => { fields.push(field.name); });

}
