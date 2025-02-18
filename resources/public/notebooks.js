htmx.onLoad(function(content) {
  var sortable = content.querySelector("#sortable");
  var sortableInstance = new Sortable(sortable, {
    handle: ".drag-handler",
    draggable: ".cell",
    animation: 150,
    ghostClass: 'bg-gray-200',
    onMove: function (evt) {
      // return evt.related.className.indexOf('htmx-indicator') === -1;
      console.log('on-move')
    },
    onEnd: function (evt) {
      console.log('end-drug')
      // this.option("disabled", true);
    }
  });

  console.log(sortable, sortableInstance);

  sortable.addEventListener("htmx:afterSwap", function() {
    sortableInstance.option("disabled", false);
  });
})

