(ns mysystem.ui.helpers
  (:require
   [system]
   [http]
   [hiccup2.core]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn hx-target [request]
  (get-in request [:headers "hx-target"]))

(defn html [x]
  (hiccup2.core/html x))

(defn hiccup [content]
  (str "<!DOCTYPE html>\n" (hiccup2.core/html content)))

(def raw hiccup2.core/raw)

(defn html-response [body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup body)})


(defn hiccup-response [request body]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup
          (if (hx-target request)
            body
            [:html
             [:head
              ;; [:script {:src "/static/tw.js"}]
              [:script {:src "/static/htmx.js"}]
              [:script {:src "/static/htmx-sse.js"}]
              [:script {:src "/static/multi-swap.js"}] ;; https://v1.htmx.org/extensions/multi-swap/
              [:link {:rel "stylesheet" :href "/static/app.build.css"}]
              [:script {:src "/static/app.js"}]
              [:meta {:charset "UTF-8"}]
              [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
              [:meta {:name "htmx-config", :content "{\"scrollIntoViewOnBoost\":false}"}]]
             [:body.text-gray-600 {:hx-boost "true" :hx-ext "sse"} body]]))})

(defn fragment [content]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup content)})

(defn menu [context request]
  [:div.my-4.p-6.flex-col
   (for [[url lbl] [["/ui/patients" "Patients"]
                    ["/ui/notebooks" "Notebooks"]
                    ["/ui/encounters" "Encounters"]
                    ["/ui/sdc" "SDC"]
                    ["/ui/tailwindui" "Tailwind"]
                    ["/ui/htmx" "htmx"]
                    ["/ui/select" "select"]
                    ["/ui/calendar" "Calendar"]
                    ["/ui/db" "db"]
                    ]]
     [:a.my-2.px-4.text-sky-600.cursor-pointer.block.border-l-4
      {:hx-push-url "true"
       :class (if (str/starts-with? (:uri request) url) "border-sky-600" "border-transparent")
       :hx-swap "outerHTML"
       :hx-get url :hx-target "#content"}
      lbl])])

(defn layout [context request fragments-map]
  (let [body [:div#content.flex.items-top
              (menu context request)
              [:div.flex-1.my-2.mx-4 {:class "grow p-4 lg:rounded-lg lg:bg-white  lg:ring-1 lg:shadow-xs lg:ring-zinc-950/5 "}
               (when-let [cnt (:content fragments-map)]
                 [:div#content.flex-1 (if (fn? cnt) (cnt) cnt)])]]]
    (if-let [trg (hx-target request)]
      (if (= "content" trg)
        {:status 200 :body (hiccup body)}
        (if-let [trg-fn (get fragments-map (keyword trg))]
          {:status 200
           :headers {"content-type" "text/html; charset=utf-8"}
           :body (hiccup (if (vector? trg-fn) trg-fn (trg-fn)))}
          (do
            (println (str "Error: no fragment for " trg))
            {:status 500 :body (str "Error: no fragment for " trg)})))
      (hiccup-response request body))))

(defn l [context request content]
  (hiccup-response request
   [:div#content.flex.items-top
    (menu context request)
    [:div.flex-1.my-2.mx-4 {:class "grow p-6 lg:rounded-lg lg:bg-white  lg:ring-1 lg:shadow-xs lg:ring-zinc-950/5 dark:lg:bg-zinc-900 dark:lg:ring-white/10"}
     content]]))


(defn href [path & [params]]
  (str "/" (str/join "/" path)
       (when params
         (str "?" (str/join "&" (mapv (fn [[k v]] (str (name k) "=" v)) params))))))

(defn table [columns rows & [row-fn]]
  (let [row-fn (or row-fn (fn [x] (->> columns (mapv (fn [k] (get x k))))))]
    (->> rows
         (mapv (fn [c] (->> (row-fn c)
                           (mapv (fn [x] [:td {:class "py-1 px-3 text-sm font-medium align-top"} x]))
                           (into [:tr {:class "hover:bg-gray-100"}]))))
         (into [:tbody {:class "divide-y divide-gray-100"}])
         (conj [:table.text-sm.mt-2
                {:class "divide-y divide-gray-300"}
                (when (seq columns)
                                 [:thead
                                  (->> columns
                                       (mapv (fn [c] [:th {:class "px-2 py-1 text-left text-sm font-semibold"}
                                                     (name c)]))
                                       (into [:tr]))])]))))

(defn js-script [name]
  (slurp (io/resource (str "scripts/"  name ".js"))))

(defn rpc [fn-name & [params]]
  (let [m (meta fn-name)
        fn-nm  (str (:ns m) "/" (:name m))]
    (str "/ui/rpc?" (http/encode-params (assoc params :method fn-nm)))))

(defn hiddens [obj]
  [:div (for [[k v] obj] [:input {:type "hidden" :name k :value v}])])

(defn btn [opts & cnt]
  (into [:button.border.rounded-md.px-4.py-1.shadow-xs.bg-white.cursor-pointer (update opts :class (fn [x] (str x " hover:bg-blue-50 text-gray-900")))] cnt))

(defn js [& content]
  [:script (raw (str/join content))])

(defn css [& content]
  [:style (raw (str/join content))])

;; (hiccup [:a {:href "ups"}])

(defn h1 [& content]
  (into [:h1 {:class "mb-4 mb-2 text-xl flex items-center space-x-4"}] content))

(defn h2 [& content]
  (into [:h2 {:class "mb-4 mb-2 text-lg flex items-center space-x-4"}] content))
