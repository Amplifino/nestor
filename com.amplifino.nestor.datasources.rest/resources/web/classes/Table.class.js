class Table {

  constructor(o, httpService) {
    this.dataSource = o && o.dataSource ? o.dataSource : 'noDataSource';
    this.name = o && o.name ? o.name : 'noTableName';
    this.fields = new Array(); // Array<Field>()
    initTableFields(this, httpService);
  }

}

function initTableFields(table, httpService) {
  httpService
    .getTableFields(table)
      .then(response => { setTableFields(table, response); })
      .catch(err => console.error('getTableFields(): ' + err));
}

function setTableFields(table, response) {
  table.fields.push(new Field(table, { name: '*'}));
  response.forEach((entry) => {
    const field = new Field(table, entry);
    table.fields.push(field);
  });

}
