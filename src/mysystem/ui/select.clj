(ns mysystem.ui.select
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [pg]
   [http]
   [clojure.string :as str]))



(def select-style 
  [:style "
.select {
  position: relative;
}

.popup {
 // display: none;
  position: absolute;
  top: 100%;
  left: 0;
}

.select input:focus + .popup {
  display: block;
}
"])



(defn select-search [context request opts]
  (let [tbl (:table opts)
        col (:column opts)
        q (:search opts)
        fmt (-> (:html opts) symbol resolve)
        items (pg/execute! context {:dsql {:select :*
                                           :from (keyword tbl)
                                           :where (when (and q (not (str/blank? q)))
                                                    [:ilike (keyword col) [:pg/param (str "%" q "%")]])}})]
    [:div {:class "divide-y"}
     (when (empty? items) [:div {:class "text-center text-gray-500"} "No items"])
     (for [it items]
       [:div {:class "px-2 cursor-pointer hover:bg-blue-100"
              :onclick "
var valel = htmx.find(htmx.closest(event.target, '.select'),'.value');
valel.innerHTML = '';
valel.appendChild(event.target.cloneNode(true));
var sel = htmx.find('#select');
htmx.addClass(sel, 'hidden');
htmx.removeClass(valel, 'hidden');
"}  (fmt it)])]))

(defn var-name [v]
  (when v
    (let [m (meta v)]
      (str (:ns m) "/" (:name m)))))

(defn select [context {tbl :table col :column :as opts}]
  [:div.select {:class "border rounded w-60"}
   [:div#value.value.hidden
    {:class "px-2 py-1 hover:bg-gray-100"
     :onclick "
var sel = htmx.find('#select');
var val = htmx.find('#value');
htmx.addClass(val, 'hidden')
htmx.removeClass(sel, 'hidden');
sel.focus()
"}
    ]
   [:input#select
    {:class "w-full px-2 py-1"
     :autocomplete "off"
     :hx-get (h/rpc #'select-search (update opts :html var-name))
     :name "search"
     :hx-target "#popup"
     :hx-trigger "keyup change delay:300"
     :placeholder "serch.."}]
   [:div#popup {:class "hidden w-full min-h-40 max-h-60 overflow-y-auto shadow-md border border-gray-100"}]
   (h/js "inputWithPopup('#select','#popup')")])


(defn table-item [tbl]
  [:div {:class ""}
   [:span (:table_name tbl)]])

(defn column-item [tbl]
  [:div {:class "flex space-x-1"}
   [:input {:name "table_name" :value (:table_name tbl)}]
   [:span {:class "text-gray-100"} (:table_name tbl)]
   [:span (:column_name tbl)]])

(defn index-html [context request]
  (let [today (java.time.LocalDate/now)]
    (h/layout
     context request
     {:content
      [:div
       ;; select-style
       (select context {:table "information_schema.tables" :column "table_name" :html #'table-item})

       ;; (select context {:table "information_schema.columns" :column "column_name" :html #'column-item})

       ]})))


(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/select"       :fn #'index-html})

  )



(comment
  (def context mysystem/context)

  (mount-routes context)


 )
