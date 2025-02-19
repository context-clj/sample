function updateCellOrder(id) {
  let cells = Array.from(document.querySelectorAll('.cell')).map((c)=> { return c.dataset.cellid;});
  // console.log('update cell order', cells)
  fetch(`/ui/notebooks/update-cell-order`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(cells)
  })
}

htmx.onLoad(function(content) {
  var sortable = content.querySelector("#sortable");
  var sortableInstance = new Sortable(sortable, {
    handle: ".drag-handler",
    draggable: ".cell",
    animation: 150,
    ghostClass: 'bg-gray-200',
    onMove: function (evt) {
      // return evt.related.className.indexOf('htmx-indicator') === -1;
      // console.log('on-move')
    },
    onEnd: function (evt) {
      updateCellOrder();
      // console.log('end-drug - update order')
    }
  });

  console.log(sortable, sortableInstance);

  sortable.addEventListener("htmx:afterSwap", function() {
    sortableInstance.option("disabled", false);
  });
})


function mdEditor(id) {
  var editor = new CodeMirror.fromTextArea(
    document.getElementById(id),
    {
      mode: 'text/x-markdown',
      autoRefresh: true,
      autoSize: true
    }
  ).setSize(null, 100);
}

function sqlEditor(id) {
  var editor = new CodeMirror.fromTextArea(
    document.getElementById(id),
    {
      mode: 'text/x-plsql',
      autoRefresh: true,
      autoSize: true
    }
  ).setSize(null, 100);
}
