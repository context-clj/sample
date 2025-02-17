(ns mysystem.ui.helpers
  (:require
   [system]
   [hiccup.core]
   [clj-yaml.core]
   [cheshire.core]
   [clojure.pprint]
   [clojure.string :as str]))

(defn hx-target [request]
  (get-in request [:headers "hx-target"]))

(defn html-response [body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup.core/html body)})

(defn hiccup-response [request body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup.core/html
          (if (hx-target request)
            body
            [:html
             [:head
              [:script {:src "https://unpkg.com/@tailwindcss/browser@4"}]
              [:script {:src "https://unpkg.com/htmx.org@2.0.4" :integrity "sha384-HGfztofotfshcF7+8n44JQL2oJmowVChPTg48S+jvZoztPfvwD79OC/LTtG6dMp+" :crossorigin "anonymous"}]
              [:style {:rel "stylesheet"} "body { font-family: sans-serif; font-size: 13px;}"]
              [:meta {:name "htmx-config", :content "{\"scrollIntoViewOnBoost\":false}"}]]
             [:body.bg-zinc-100.text-gray-600 {:hx-boost "true"} body]]))})

(defn layout [_context request fragments-map]
  (if-let [trg (hx-target request)]
    (if-let [trg-fn (get fragments-map (keyword trg))]
      {:status 200 :body (hiccup.core/html (trg-fn))}
      (do
        (println (str "Error: no fragment for " trg))
        {:status 500 :body (str "Error: no fragment for " trg)}))
    (hiccup-response
     request
     [:div.flex-1.my-2.mx-4 {:class "grow p-6 lg:rounded-lg lg:bg-white  lg:ring-1 lg:shadow-xs lg:ring-zinc-950/5 dark:lg:bg-zinc-900 dark:lg:ring-white/10"}
      (when-let [cnt (:content fragments-map)]
        [:div#content.flex-1 (if (fn? cnt) (cnt) cnt)])])))


(defn layout [_context request fragments-map]
  (if-let [trg (hx-target request)]
    (if-let [trg-fn (get fragments-map (keyword trg))]
      {:status 200 :body (hiccup.core/html (trg-fn))}
      (do
        (println (str "Error: no fragment for " trg))
        {:status 500 :body (str "Error: no fragment for " trg)}))
    (hiccup-response
     request
     [:div.flex.items-top.bg-zinc-100
      [:div.flex-1.my-2.mx-4
       {:class "grow p-6 lg:rounded-lg lg:bg-white  lg:ring-1 lg:shadow-xs lg:ring-zinc-950/5 dark:lg:bg-zinc-900 dark:lg:ring-white/10"}
       (when-let [tnav (:topnav fragments-map)]
         [:div#topnav.border-r (if (fn? tnav) (tnav) tnav)])
       (when-let [nav (:navigation fragments-map)]
         [:div#nav.border-r (if (fn? nav) (nav) nav)])
       (when-let [cnt (:content fragments-map)]
         [:div#content.flex-1 (if (fn? cnt) (cnt) cnt)])]])))
