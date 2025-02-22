function isObject(value) {
  return value
    && typeof value === 'object'
    && !Array.isArray(value);
}

function isString(value) {
  return typeof value === 'string'
}

function assoc(obj, path, value) {
  let current = obj
  let k = null;
  let len = path.length;
  for (let i = 0; i < len; i++) {
    k  = path[i]
    nk = path[i+1]
    if(isString(k)) {
      current[k] = (i == (len - 1)) ? value : ( current[k] || ( Number.isInteger(nk) ? [] : ( isObject(nk) ? nk : {})))
    } else if (Number.isInteger(k)) {
      for(let j = current.length; j < k; j++) { current[j] = current[j]  || null; }
      current[k] = (i == (len - 1)) ? value : ( current[k] || ( Number.isInteger(nk) ? [] : ( isObject(nk) ? nk : {})))
    }
    if(!isObject(k)) {
      current = current[k]
    }
  }
}

window.form = {resourceType:  "QuestionnaireResponse"};


const cleanup = (obj) => {
  if (Array.isArray(obj)) {
    return obj
      .filter(v => v != null)
      .map(v => cleanup(v));
  }
  if (typeof obj === 'object' && obj !== null) {
    return Object.fromEntries(
      Object.entries(obj)
        .filter(([_, v]) => v != null)
        .map(([k, v]) => [k, cleanup(v)])
    );
  }
  return obj;
}

function calculateExpressions(resource){

  htmx.ajax('POST', '/ui/sdc/preview', {
    target: '#server-result',
    values: new FormData(document.querySelector('#form form')),
    // headers: {'X-Custom': 'value'},
    // indicate: true,
    // select: '#specific-part'
  })

  var envVars = {resource: resource};
  document.querySelectorAll("#form [data-fhirpathname]").forEach((node) => {
    let exp = node.dataset.fhirpathexpr;
    let nm = node.dataset.fhirpathname;
    let trg = node.dataset.target;
    let res = fhirpath.evaluate(resource, exp, envVars);
    envVars[nm] = res;
    // console.log('eval', nm, exp, '=>', res, trg)
    if(res.length == 0) {
      document.getElementById(trg).innerHTML = ''
    } else {
      document.getElementById(trg).innerHTML = JSON.stringify(res)
    }
  })
}

document.querySelector("#form")?.addEventListener('change', (event) => {
  var el = event.target;
  let path = JSON.parse(el.dataset.path);
  // console.log(el.dataset.linkid, "assoc", path , el.value);
  assoc(window.form, path, el.value)
  document.getElementById('debug').innerHTML = JSON.stringify(cleanup(window.form), null, " ");
  calculateExpressions(window.form)
})

// var obj = {}
// assoc(obj, ['a','b','c'], 1)
// assoc(obj, ['a','d', 0,'c'], 1)
// assoc(obj, ['a','d', 5,'c'], 1)
// assoc(obj, ['a','x', {sys:'s'},'v'], 1)

// console.log(JSON.stringify(obj,null, " "))
