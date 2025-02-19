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
