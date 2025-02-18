function updateCellOrder(id) {
  let cells = Array.from(document.querySelectorAll('.cell')).map((c)=> { return c.dataset.cellid;});
  console.log('update cell order', cells)
  fetch(`/ui/notebooks/update-cell-order`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(cells)
  })
}
