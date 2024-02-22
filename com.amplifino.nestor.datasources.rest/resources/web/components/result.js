function resultCtrl($scope) {
  this.$onInit = function() {
    $scope.ui = this.ui;
  }
}

app.component('result', {
  templateUrl: './components/result.html',
  controller: resultCtrl,
  controllerAs: '$scope',
  bindings: { ui: '=' },
});

class Result {

  constructor(o) {
    this.rowCount = o && o.rowCount ? o.rowCount : 0;
    this.columns = o && o.columns ? o.columns : {};
    this.tuples = o && o.tuples ? o.tuples : new Array();
  }

  getData(row, col) {
    const cTable = col.table;
    const rTable = row[cTable];
    if (!rTable) return '';
    const cName = col.name;
    const rCol = rTable[cName];
    return rCol;
  }

  export() {
    const fileOptions = {
      format: 'csv',
      type: 'text/csv;charset=utf-8',
    }
    // const fileOptions = {
    //   format: 'xlsx',
    //   type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    // }
    const exportOptions = {
      headers: true,                      // (Boolean), display table headers (th or td elements) in the <thead>, (default: true)
      footers: true,                      // (Boolean), display table footers (th or td elements) in the <tfoot>, (default: false)
      formats: [ fileOptions.format ],    // (String[]), filetype(s) for the export, (default: ['xlsx', 'csv', 'txt'])
      filename: "id",                     // (id, String), filename for the downloaded file, (default: 'id')
      bootstrap: false,                   // (Boolean), style buttons using bootstrap, (default: true)
      exportButtons: false,               // (Boolean), automatically generate the built-in export buttons for each of the specified formats (default: true)
      position: "bottom",                 // (top, bottom), position of the caption element relative to table, (default: 'bottom')
      ignoreRows: null,                   // (Number, Number[]), row indices to exclude from the exported file(s) (default: null)
      ignoreCols: null,                   // (Number, Number[]), column indices to exclude from the exported file(s) (default: null)
      trimWhitespace: true,               // (Boolean), remove all leading/trailing newlines, spaces, and tabs from cell text in the exported file(s) (default: false)
      RTL: false,                         // (Boolean), set direction of the worksheet to right-to-left (default: false)
      sheetname: "adHocTable"             // (id, String), sheet name for the exported spreadsheet, (default: 'id')
    };
    const id = 'exportableResultTable';
    const table = TableExport(document.getElementById(id), exportOptions);
    const data = table.getExportData();
    const fileContent = data[id][fileOptions.format];
    const blob = new Blob([ fileContent.data ], { type: fileOptions.type } );
    saveAs(blob, 'adHocTable.' + fileOptions.format);
  }

}
