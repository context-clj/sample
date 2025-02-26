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
    htmx.trigger(inp, 'open')
  });

  inp.addEventListener('blur', () => {
    setTimeout(()=>{
      // console.log('close', inp, pop)
      pop.classList.toggle('hidden', true);
      // todo clear popup
    }, 100)
  });

  var currentIndex = 0;

  function selectDiv(idx,divs) {
    console.log('selection', idx);
    htmx.findAll('.select-item').forEach((d)=>{
      d.classList.remove('selected');
    })
    divs[idx].classList.add('selected')
    divs[idx].scrollIntoViewIfNeeded()
    console.log(divs[idx].classList)
    currentIndex = idx;
  }

  inp.addEventListener('keydown', function(event) {
    let selectableDivs = htmx.findAll('.select-item');
    // console.log(selectableDivs)
    switch(event.key) {
    case 'ArrowDown':
      event.preventDefault();
      event.stopPropagation();
      if (currentIndex === -1) {
        selectDiv(0, selectableDivs);
      } else {
        selectDiv((currentIndex + 1) % selectableDivs.length, selectableDivs);
      }
      break;
    case 'ArrowUp':
      event.preventDefault();
      event.stopPropagation();
      if (currentIndex === -1) {
        selectDiv(selectableDivs.length - 1);
      } else {
        selectDiv((currentIndex - 1 + selectableDivs.length) % selectableDivs.length, selectableDivs);
      }
      break;
    case 'Enter':
      if (currentIndex !== -1) {
        event.preventDefault();
        event.stopPropagation();
        selectableDivs[currentIndex].click();
      }
      break;
    case 'Escape':
      resetSelection();
      currentIndex = -1;
      break;
    default:
      htmx.trigger(inp, 'keyupup')
    }
  });
}

