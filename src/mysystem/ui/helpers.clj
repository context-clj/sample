(ns mysystem.ui.helpers
  (:require
   [system]
   [hiccup.core]
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
              [:script {:src "/static/tw.js"}]
              [:script {:src "/static/htmx.js"}]
              [:style {:rel "stylesheet"} "body { font-family: sans-serif; font-size: 13px;}"]
              [:meta {:name "htmx-config", :content "{\"scrollIntoViewOnBoost\":false}"}]]
             [:body.bg-zinc-100.text-gray-600 {:hx-boost "true"} body]]))})

(defn fragment [content]
  {:status 200
   :headers {"content-type" "text/html"}
   :body (hiccup.core/html content)})

(defn menu [context request]
  [:div.my-4.p-6.flex-col
   (for [[url lbl] [["/ui/patients" "Patients"]
                    ["/ui/notebooks" "Notebooks"]
                    ["/ui/encounters" "Encounters"]]]
     [:a.my-2.px-4.text-sky-600.cursor-pointer.block.border-l-4
      {:hx-push-url "true"
       :class (if (str/starts-with? (:uri request) url) "border-sky-600" "border-transparent")
       :hx-swap "outerHTML"
       :hx-get url :hx-target "#content"}
      lbl])])

(defn layout [context request fragments-map]
  (let [body [:div#content.flex.items-top
              (menu context request)
              [:div.flex-1.my-2.mx-4 {:class "grow p-6 lg:rounded-lg lg:bg-white  lg:ring-1 lg:shadow-xs lg:ring-zinc-950/5 dark:lg:bg-zinc-900 dark:lg:ring-white/10"}
               (when-let [cnt (:content fragments-map)]
                 [:div#content.flex-1 (if (fn? cnt) (cnt) cnt)])]]]
    (if-let [trg (hx-target request)]
      (if (= "content" trg)
        {:status 200 :body (hiccup.core/html body)}
        (if-let [trg-fn (get fragments-map (keyword trg))]
          {:status 200 :body (hiccup.core/html (if (vector? trg-fn) trg-fn (trg-fn)))}
          (do
            (println (str "Error: no fragment for " trg))
            {:status 500 :body (str "Error: no fragment for " trg)})))
      (hiccup-response request body))))


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
                                       (into [:tr])
                                       )]
                                 )]))))
