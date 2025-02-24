(ns mysystem.ui.db
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [pg]
   [cheshire.core]
   [clojure.string :as str]
   [http]))

(def drop-down-css
  [:style
   "
.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: absolute;
  z-index: 1;
}

.dropdown-content a:hover {background-color: #ddd;}
.dropdown:hover .dropdown-content {display: block;}
"])

(defn tables-list [context {{q :search} :query-params {tsch :schema ttbl :table} :route-params :as request} & [params]]
  (let [tables (pg/execute! context {:dsql {:select :* :from :information_schema.tables
                                            :where (when (and q (not (str/blank? q))) [:ilike :table_name [:pg/param (str "%" q "%")]])
                                            :order-by [:pg/list :table_schema :table_name]}})]
    [:div
     (for [[sch tables] (group-by :table_schema tables)]
       [:details {:open (or q (= sch tsch))}
        [:style ".menu-active { font-weight: bold }"]
        [:summary {:class "px-4 py-1 border-b hover:pg-gray-100 font-bold bg-gray-100"} sch]
        [:div {:class "border-b"}
         (for [tbl tables]
           [:a {:hx-get (str "/ui/db/tables/" sch "/" (:table_name tbl))
                :hx-target "#page"
                :hx-push-url "true"
                :onclick "document.querySelectorAll('.menu-active').forEach((x)=> x.classList.remove('menu-active')); event.target.classList.add('menu-active'); "
                :class ["block pl-6 py-1 text-xs cursor-pointer hover:bg-gray-100"
                        (when (= (:table_name tbl) ttbl) "menu-active")]}
            (:table_name tbl)])]])]))

(defn tables-view [context request & [params]]
  [:div {:class "h-screen overflow-y-auto"}
   [:input {:class "border-b px-4 py-2 border-b w-full bg-yellow-50"
            :placeholder "filter.."
            :name "search"
            :hx-target "#tables-list"
            :hx-trigger "keyup changed delay:300ms"
            :hx-get (h/rpc #'tables-list)}]
   [:div#tables-list (tables-list context request params)]])

(defn layout [context request content params]
  (h/hiccup-response
   context
   [:div {:class "flex min-h-screen"}
    [:div {:class "min-w-60 w-60 bg-gray-100 border-r bg-white"}
     (tables-view context request params)]
    [:div#page {:class "flex-1"} content]]))

(defn columns-selector [context {{sch :schema tbl :table} :route-params :as request}]
  (let [columns (pg/execute! context {:dsql {:select [:pg/list :ordinal_position :data_type :column_name]
                                             :from :information_schema.columns
                                             :order-by :ordinal_position
                                             :where {:match
                                                     [:and
                                                      [:= :table_schema sch]
                                                      [:= :table_name tbl]]}}})]
    [:div {:class "inline-block text-sm"}
     drop-down-css
     [:form {:hx-get (:uri request)
             :hx-trigger "change"
             :hx-replace-url "true"
             :hx-include "[name=column]"
             :hx-target "#table-data"}
      [:div.dropdown
       [:div {:class "px-4 py-2 border hover:bg-gray-100 cursor-pointer rounded-md"} "Columns"]
       [:div.dropdown-content {:class "border shadow-md bg-white text-xs"}
        (for [c columns]
          [:label {:class "block border-b flex space-x-1 cursor-pointer hover:bg-gray-100 px-2 py-1"}
           [:input {:type "checkbox" :name "column" :value (:column_name c)}]
           [:span (:column_name c)]])]]]]))

(defn table-data [context {{sch :schema tbl :table} :route-params {select-columns :column} :query-params :as request}]
  (let [select-columns (if (string? select-columns) [select-columns] select-columns)
        columns (pg/execute! context {:dsql {:select [:pg/list :ordinal_position :data_type :column_name]
                                             :from :information_schema.columns
                                             :order-by :ordinal_position
                                             :where {:match
                                                     [:and
                                                      [:= :table_schema sch]
                                                      [:= :table_name tbl]]
                                                     :columns (when select-columns
                                                                [:in :column_name [:pg/params-list select-columns]])}}})
        columns-idx (group-by :column_name columns)
        rows (pg/execute! context {:dsql {:select (if select-columns (into [:pg/list] (mapv keyword select-columns)) :*)
                                          :from (keyword (str sch "." tbl))
                                          :limit 50}})
        columns (mapv (fn [c] (get-in columns-idx [(name c) 0])) (keys (first rows)))]
    [:div
     ;; [:pre (pr-str (:query-params request))]
     [:table
      [:thead
       [:tr {:class "border-b"}
        (for [c columns]
          [:th {:class "px-2 py-1 font-semibold text-gray-500 text-left"}
           (:column_name c)])]]
      [:tbody
       (for [r rows]
         [:tr {:class "border-b hover:bg-gray-100"}
          (for [{tp :data_type :as c} columns]
            [:td {:class ["px-2 py-1 text-gray-600 text-xs text-top" (when (contains? #{"integer"} tp) "text-right")]}
             (let [v (get r (keyword (:column_name c)))]
               (cond
                 (contains? #{"text" "character varying" "char" "\"char\"" "uuid"} tp) [:span {:class "text-gray-700"} v]
                 (contains? #{"name" "oid"} tp) [:span {:class "text-pink-900"} v]
                 (contains? #{"timestamp without time zone" "timestamp with time zone"} tp) (str v)
                 (contains? #{"integer" "bigint" "real" "numeric" "smallint" "xid"} tp) [:span {:class "text-blue-600"} v]
                 (contains? #{"boolean"} tp) [:span {:class "text-blue-600"} (str v)]
                 (contains? #{"jsonb"} tp) [:pre (cheshire.core/generate-string v {:pretty true})]
                 (contains? #{"ARRAY"} tp) (str "[" (->> v (mapv str) (str/join ", ")) "]")
                 (nil? v) [:span {:class "text-gray-500"} "NULL"]
                 :else
                 [:div [:b (pr-str (:data_type c))] " " (pr-str v)])
               )])])]]]))

(defn table-html [context {{sch :schema tbl :table :as rp} :route-params :as request}]
  (let [trg (get-in request [:headers "hx-target"])
        layout (if trg h/fragment #(layout context request % rp))]
    (cond
      (= "table-data" trg) (layout [:div#table-data (table-data context request)])
      :else (layout
             [:div {:class "p-4 bg-white"}
              (h/h1 [:span tbl] (columns-selector context request))
              [:div#table-data (table-data context request)]]))))

(defn index-html [context request]
  (h/hiccup-response
   context
   [:div {:class "flex min-h-screen"}
    [:div {:class "w-80 bg-gray-100 border-r bg-white"}
     (tables-view context request {})]
    [:div#page {:class "flex-1"}]]))


(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/db"       :fn #'index-html})
  (http/register-endpoint context {:method :get :path  "/ui/db/tables/:schema/:table"       :fn #'table-html})
  )



(comment
  (def context mysystem/context)

  (mount-routes context)

 )
