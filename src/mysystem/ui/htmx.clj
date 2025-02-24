(ns mysystem.ui.htmx
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [http]
   [clojure.string :as str]
            [rewrite-clj.node :as n]
   [cljfmt.core :as cljfmt]))



;; TODO: generate all this combinations

(defn datetime [context request opts]
  [:div (second (str/split (pr-str (java.util.Date.)) #"(T|\.)"))])

(comment
  (datetime context {} {})

  )

(defn script [& cnt]
  [:script (h/raw (str/join cnt))])

(defn code-block [lang data]
  (let [id (str (gensym))]
    [:pre.my-4.p-2.text-xs.bg-gray-100
     [:code {:id id :class (str "language-" (name lang))} data]
     (script "hljs.highlightElement(window.document.getElementById('" id "'))")]))

(defn load-more [context request {p :page}]
  (let [p (if (string? p) (Integer/parseInt p) p)
        size 5]
    [:div
     (for [i (range (* size p) (+ (* size p) 5))]
       [:div {:class "border-b py-1"} (pr-str "#" i)])
     (h/btn {:hx-swap "outerHTML" :hx-post (h/rpc #'load-more {:page (inc p)})} "load more")]))


(defn index-html [context request]
  (h/layout
   context request
   {:content
    [:div

     [:link   {:rel "stylesheet", :href "/static/hljs.css"}]
     [:link   {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js" :integrity "sha512-EBLzUL8XLl+va/zAsmXwS7Z2B1F9HUHkZwyS/VKwh3S7T/U0nF4BaU29EP/ZSf6zgiIxYAnKLu6bJ8dqpmX5uw==" :crossorigin "anonymous" :referrerpolicy "no-referrer"}]     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"}]
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/highlight.min.js" :integrity "sha512-EBLzUL8XLl+va/zAsmXwS7Z2B1F9HUHkZwyS/VKwh3S7T/U0nF4BaU29EP/ZSf6zgiIxYAnKLu6bJ8dqpmX5uw==" :crossorigin "anonymous" :referrerpolicy "no-referrer"}]     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"}]
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.0/languages/clojure.min.js"}]

     (h/h1 "HTMX")

     (h/h2 "Polling")

     [:div {:class "border rounded p-4 shadow-md text-xl font-bold bg-yellow-100" :hx-get (h/rpc #'datetime) :hx-trigger "every 1s"}
      (datetime context request {})]

     (code-block "clojure"
                 (str
                  (pr-str '[:div {:hx-get (h/rpc #'datetime) :hx-trigger "every 1s"}])
                  "\n
(defn datetime [context request opts]
    [:div (second (str/split (pr-str (java.util.Date.)) #\"(T|\\.)\"))])
"))

     (h/h2 "Load more")
     (code-block "clojure"
                 (str
                  (pr-str '[:div (load-more context request {:page 0})])
                  "\n
(defn load-more [context request {p :page}]
  (let [p (if (string? p) (Integer/parseInt p) p)
        size 5]
    [:div
      (for [i (range (* size p) (+ (* size p) 5))]
        [:div {:class \"border-b py-1\"} (pr-str \"#\" i)])
      (h/btn {:hx-swap \"outerHTML\" :hx-post (h/rpc #'load-more {:page (inc p)})} \"load more\")]))"))

     [:div
      (load-more context request {:page 0})]]}))





(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/htmx"       :fn #'index-html})

  )



(comment
  (def context mysystem/context)

  (mount-routes context)

  

  (cljfmt/reformat-string "(defn a [x y]
 (let [a 1](+ x y)))")

 )
