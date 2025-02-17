function select(event){
  var target = event.target;
  target.classList.add('bg-blue-100')
  var fld = target.dataset.field;
  document.querySelector(`input[name=\"${fld}\"]`).value = target.dataset.value
  document.querySelectorAll(`[data-field=\"${fld}\"]`).forEach((el)=> {
    if (el != target) {
      el.classList.remove('bg-blue-100')
    }
  })
}
