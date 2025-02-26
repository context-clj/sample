//https://github.com/bigskysoftware/htmx/issues/2156
htmx.defineExtension('push-url-params', {
  onEvent : function(name, e) {
    if (name === "htmx:configRequest") {
      window.event = e.target
      const path = e.target.getAttribute('data-push-url') || window.location.pathname;
      const params = new URLSearchParams(e.detail.parameters.entries()).toString()
      const url = `${path}?${params}`
      window.history.pushState({}, '', url);
    }
  }
})

function inputWithPopup(id, pid) {
  var inp = htmx.find(id)
  var pop = htmx.find(pid)
  // console.log('init', inp, pop)

  inp.addEventListener('focus', () => {
    // console.log('open', inp, pop)
    pop.classList.toggle('hidden', false);
  });

  inp.addEventListener('blur', () => {
    setTimeout(()=>{
      // console.log('close', inp, pop)
      pop.classList.toggle('hidden', true);
    }, 100)
  });
}

function selectItem(ev) {
  console.log(ev.target, 'select')
}
