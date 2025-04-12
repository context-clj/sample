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

function byid(id) {
    return document.getElementById(id);
}
function expandHeight(targ) {
    var pane = byid(targ.dataset.expand);
    if(!pane) {
        console.error('data-expand: can not find element', targ, targ.dataset.expand)
        return;
    }

    console.log('show/hide', targ.dataset.expandshow)
    if( pane.dataset.expanded != 'on') {
        pane.dataset.expanded = 'on'
        targ.dataset.open = 'on'
    } else {
        pane.dataset.expanded = 'off'
        targ.dataset.open = 'off'
    }
}

function copyFrom(targ) {
    navigator.clipboard.writeText(byid(targ.dataset.copyfrom).innerText)
    targ.dataset.copied = 'yes'
    setTimeout(()=> { targ.dataset.copied = '' }, 2000)
}

function toggle(targ) {
    var el = byid(targ.dataset.toggle)
    var grp = targ.dataset.togglegroup;

    if(!!grp) {
        document.querySelectorAll(`[data-togglegroup='${grp}']`).forEach((el)=>{
            if(el != targ) {
                var tel = byid(el.dataset.toggle);
                if(tel){
                    tel.dataset.hidden = 'yes'
                }
            }
        })
    }


    if(el.dataset.hidden == "yes") {
        el.dataset.hidden = ''
    } else {
        el.dataset.hidden == "yes"
    }
}

document.addEventListener('click', (e) => {
    // console.log('Clicked:', e.target);
    var targ = event.target.closest('[data-expand]');
    if(targ) { expandHeight(targ) }

    var ctarg = event.target.closest('[data-copyfrom]');
    if(ctarg) { copyFrom(ctarg) }

    var ttarg = event.target.closest('[data-toggle]');
    if(ttarg) { toggle(ttarg) }

}, true);


if (!window._debounceTimers) {
    window._debounceTimers = {};
}
function debounce_ajax(method, uri, opts, wait, channel) {
  clearTimeout(window._debounceTimers[channel]);
  window._debounceTimers[channel] = setTimeout(function () {
    htmx.ajax(method, uri, opts);
  }, wait);
}

htmx.defineExtension('publish-event', {
  init : function(api) {
    console.log('publish-event init')
  },
  onEvent : function(name, e) {
    if(e.type == 'htmx:afterProcessNode') {
      const target = e.target;
      if (target) {
        const eventName = target.getAttribute('publish-event');
        target.addEventListener('keyup', (e) => {
          debounce_ajax('GET', '/ui/chat/printing', {target: '#null'}, 300, eventName)
        })
      }
    }
  }
})

htmx.defineExtension('remove-in', {
  onEvent : function(name, e) {
    //console.log('event', e, e.target, e.type)
    if(e.type == 'htmx:load') {
      setTimeout(() => {
        e?.target?.remove();
      }, 1000);
    }
  }
})

htmx.defineExtension('scroll-to-bottom', {
  onEvent : function(name, e) {
    //console.log('scroll-to-bottom', e.type, e.target)
    const target = e.target;
    if( target && (e.type == 'htmx:afterSwap' || e.type == 'htmx:afterProcessNode') ){
      const id = target.getAttribute( "scroll-to-bottom") 
      //console.log('scroll', id)
      const el = htmx.find(id);
      if(el) {
        //console.log('scroll', el, el.scrollHeight)
        setTimeout(() => {
          el.scrollTo({top: el.scrollHeight, behavior: 'smooth' });
        }, 100)
      }
    }
  }
})