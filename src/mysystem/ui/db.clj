(ns mysystem.ui.db
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [pg]
   [cheshire.core]
   [clojure.string :as str]
   [htmx]
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

(defn filter-icon [size]
  [:svg {:class size :xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"} [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v1.044a2.25 2.25 0 0 1-.659 1.591l-5.432 5.432a2.25 2.25 0 0 0-.659 1.591v2.927a2.25 2.25 0 0 1-1.244 2.013L9.75 21v-6.568a2.25 2.25 0 0 0-.659-1.591L3.659 7.409A2.25 2.25 0 0 1 3 5.818V4.774c0-.54.384-1.006.917-1.096A48.32 48.32 0 0 1 12 3Z"}]])

(defn trash-icon [size]
  [:svg {:class size :xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"} [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0"}]])

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
           [:a {htmx/get (str "/ui/db/tables/" sch "/" (:table_name tbl))
                htmx/target "#page"
                htmx/push-url "true"
                :onclick "document.querySelectorAll('.menu-active').forEach((x)=> x.classList.remove('menu-active')); event.target.classList.add('menu-active'); "
                :class ["block pl-6 py-1 text-xs cursor-pointer hover:bg-gray-100"
                        (when (= (:table_name tbl) ttbl) "menu-active")]}
            (:table_name tbl)])]])]))

(defn tables-view [context request & [params]]
  [:div {:class "h-screen overflow-y-auto"}
   [:input {:class "border-b px-4 py-2 border-b w-full bg-yellow-50"
            :placeholder "filter.."
            :name "search"
            htmx/target "#tables-list"
            htmx/trigger "keyup changed delay:300ms"
            htmx/get (h/rpc #'tables-list)}]
   [:div#tables-list (tables-list context request params)]])

(defn layout [context request content params]
  (h/hiccup-response
   context
   [:div {:class "flex min-h-screen"}
    [:div {:class "min-w-60 w-60 bg-gray-100 border-r bg-white"}
     (tables-view context request params)]
    [:div#page {:class "flex-1"} content]]))

(defn remove-form-item []
  [:button {:class "px-2 text-red-500"
            :onclick "htmx.remove(htmx.closest(this, '.to-rm')); htmx.trigger('#filter-form', 'submit')"}
   (trash-icon "size-4 text-red-50 group-hover:text-red-500")])



(defn add-column [context request params]
  [:div.to-rm {:class "group px-2 flex items-center space-x-1 border rounded bg-white"}
   [:b {:type (:data_type params)} (:column_name params)]
   [:input  {:class "px-4 border py-1 rounded w-60"
             :type :hidden
             :name "column"
             :value (:column_name params)}]
   (remove-form-item)])

(defn add-filter [context request params & [filter]]
  [:tr.to-rm {:class "group bg-white"}
   [:td {:class "px-4"} [:b {:type (:data_type params)} (:column_name params)]
    [:input {:type "hidden" :name "$"}]
    [:input {:type "hidden" :name "col"   :value (:column_name params)}]
    [:input {:type "hidden" :name "type"  :value (:data_type params)}]]
   [:td
    [:select {:class "px-2 py-1" :name "op"}
     (for [op ["=" "!=" "ilike"]]
       [:option {:value op :selected (= op (get filter "op"))} op])]]
   [:td.to-rm
    {:class "pl-4"} [:input  {:type "text"
                              :class "px-4 border py-1 rounded w-60"
                              :name "value"
                              :value (or (get filter "value") (:value params))}]]
   [:td (remove-form-item)]])

(defn parse-filters [qs]
  (when qs
    (->> (str/split qs #"%24=&")
         (rest)
         (mapv http/form-decode))))

(defn table-panel [context {{column :column :as qp} :query-params qs :query-string :as request}]
  [:form#filter-form
   {htmx/get (:uri request)
    htmx/replace-url "true"
    htmx/trigger "keyup change delay:300ms, submit preventDefault"
    htmx/target "#table-data"
    (htmx/on htmx/after-swap) "htmx.trigger('#filter-form', 'submit')"}
   [:div {:class "bg-gray-100 p-4"}
    [:div#table-columns {:class "flex space-x-2 items-center"}
     (for [col (if (string? column) [column] column)]
       (add-column context request {:column_name col}))]
    [:table {:class "my-2 border"}
     [:tbody#table-filters {}
      (for [filter (parse-filters qs)]
        (add-filter context request {:column_name (get filter "col") :data_type (get filter "type")} filter))]]]
   [:button {:type :submit} "submit"]])

(defn columns-selector [context {{sch :schema tbl :table} :route-params :as request}]
  (let [columns (pg/execute! context {:dsql {:select [:pg/list :ordinal_position :data_type :column_name]
                                             :from :information_schema.columns
                                             :order-by :ordinal_position
                                             :where {:match
                                                     [:and
                                                      [:= :table_schema sch]
                                                      [:= :table_name tbl]]}}})]
    [:div {:class "flex space-x-4"}
      drop-down-css
     [:div {:class "inline-block text-sm"}
      [:div.dropdown
       [:div {:class "px-4 py-2 border hover:bg-gray-100 cursor-pointer rounded-md"} "Columns"]
       [:div.dropdown-content {:class "border shadow-md bg-white text-xs"}
        (for [c columns]
          [:a {:class "block border-b cursor-pointer hover:bg-gray-100 px-2 py-1"
               htmx/get (h/rpc #'add-column c)
               htmx/target "#table-columns"
               htmx/swap "beforeend"}
           (:column_name c)])]]]
     [:div {:class "inline-block text-sm"}
      [:div.dropdown
       [:div {:class "px-4 py-2 border hover:bg-gray-100 cursor-pointer rounded-md"} "Add filter"]
       [:div.dropdown-content {:class "border shadow-md bg-white text-xs"}
        (for [c columns]
          [:a {:class "block border-b cursor-pointer hover:bg-gray-100 px-2 py-1"
               htmx/get (h/rpc #'add-filter c)
               htmx/target "#table-filters"
               htmx/swap "beforeend"}
           (:column_name c)])]]]]))


(defn table-data [context {{sch :schema tbl :table} :route-params {select-columns :column :as qs} :query-params :as request}]
  (let [select-columns (if (string? select-columns) [select-columns] select-columns)
        filters (parse-filters (:query-string request))
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
        query {:select (if select-columns (into [:pg/list] (mapv keyword select-columns)) :*)
               :from (keyword (str sch "." tbl))
               :where (->> filters
                           (reduce (fn [acc {o "op" v "value" col "col"}]
                                     (if (and v (not (str/blank? v)))
                                       (cond
                                         (= o "ilike")
                                         (assoc acc col [:ilike (keyword col) [:pg/param (str "%" v "%")]])
                                         :else
                                         (assoc acc col [:= (keyword col) [:pg/param v]]))
                                       acc)
                                     ) {})) :limit 50}
        rows (pg/execute! context {:dsql query})
        columns (mapv (fn [c] (get-in columns-idx [(name c) 0])) (keys (first rows)))]
    [:div {:class "mt-4"}
     [:details
      [:summary "sql"]
      (pr-str query)]
     [:table
      [:thead
       [:tr {:class "border-b"}
        (for [c columns]
          [:th {:class "font-semibold text-gray-500 text-left"}
           [:div {:class "flex items-center"}
            [:span {:class "pl-2 py-2"}(:column_name c)]
            [:a {:class "cursor-pointer hover:text-sky-400 px-2 py-2 rounded"
                 htmx/get (h/rpc #'add-filter c)
                 htmx/target "#table-filters"
                 htmx/swap "beforeend"}
             (filter-icon "size-4")]]])]]
      [:tbody
       (for [r rows]
         [:tr {:class "border-b hover:border-gray-400"}
          (for [{tp :data_type :as c} columns]
            [:td {:class ["group px-2 py-1 text-gray-600 text-xs text-top" (when (contains? #{"integer"} tp) "text-right")]}
             (let [v (get r (keyword (:column_name c)))]
               [:div {:class "flex items-center"}
                [:div {:class "flex-1"}
                 (cond
                   (nil? v) [:span {:class "text-gray-500"} "NULL"]
                   (contains? #{"text" "character varying" "char" "\"char\"" "uuid"} tp) [:span {:class "text-gray-700"} v]
                   (contains? #{"name" "oid"} tp) [:span {:class "text-pink-900"} v]
                   (contains? #{"timestamp without time zone" "timestamp with time zone"} tp) (str v)
                   (contains? #{"integer" "bigint" "real" "numeric" "smallint" "xid"} tp) [:span {:class "text-blue-600"} v]
                   (contains? #{"boolean"} tp) [:span {:class "text-blue-600"} (str v)]
                   (contains? #{"jsonb"} tp) [:details [:summary (pr-str (keys v))] [:pre (cheshire.core/generate-string v {:pretty true})]]
                   (contains? #{"ARRAY"} tp) (str "[" (->> v (mapv str) (str/join ", ")) "]")
                   :else
                   [:div [:b (pr-str (:data_type c))] " " (pr-str v)])]
                [:a {:class "cursor-pointer text-gray-50 hover:text-sky-400 px-2 py-2 rounded group-hover:text-sky-500"
                     htmx/get (h/rpc #'add-filter (assoc c :value (str v)))
                     htmx/push-url "false"
                     htmx/target "#table-filters"
                     htmx/swap "beforeend"}
                 (filter-icon "size-3")]])])])]]]))



(defn table-html [context {{sch :schema tbl :table :as rp} :route-params :as request}]
  (let [trg (get-in request [:headers "hx-target"])
        layout (if trg h/fragment #(layout context request % rp))]
    (cond
      (= "table-data" trg) (layout [:div#table-data (table-data context request)])
      :else (layout
             [:div {:class "p-4 bg-white"}
              (h/h1 [:span tbl] (columns-selector context request))
               (table-panel context request)
              
              [:div#table-data (table-data context request)]
              ]))))

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
