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
  position: absolute;
  top: 100%;
  left: 0;
}

.select-item.selected {
  background-color: oklch(0.97 0.014 254.604);
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
                                                    [:ilike (keyword col) [:pg/param (str "%" q "%")]])
                                           :order-by (keyword col)
                                           :limit 25}})]
    [:div {:class "divide-y"}
     (when (empty? items) [:div {:class "text-center text-gray-500"} "No items"])
     (for [it items]
       [:div.select-item
        {:class "px-2 cursor-pointer hover:bg-blue-100"
         :onclick "
var valel = htmx.find(htmx.closest(event.target, '.select'),'.value');
valel.innerHTML = '';
var clonel = htmx.closest(event.target,'.select-item');
valel.appendChild(clonel.firstChild.cloneNode(true));
var sel = htmx.find('#select');
htmx.find('#popup').innerHTML = ''
htmx.find('#select').value = ''
htmx.addClass(sel, 'hidden');
htmx.removeClass(valel, 'hidden');
"}  (fmt it)])]))

(defn var-name [v]
  (when v
    (let [m (meta v)]
      (str (:ns m) "/" (:name m)))))

(defn chevron [& [size]]
  [:svg {:class (or size "size-4") :xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"} [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "m19.5 8.25-7.5 7.5-7.5-7.5"}]])

(defn select [context {tbl :table col :column :as opts}]
  [:div.select {:class "border rounded min-w-80  hover:bg-gray-100"}
   [:div {:class "w-full flex space-x-1 items-center px-2"}
    [:div#value.value.hidden
     {:class "flex-1 px-2 py-1"
      :onclick "
var sel = htmx.find('#select');
var val = htmx.find('#value');
htmx.addClass(val, 'hidden')
htmx.removeClass(sel, 'hidden');
sel.focus()
"}
     ]
    [:input#select
     {:class "flex-1 px-2 py-2 focus:outline-none bg-transparent"
      :form ""
      :autocomplete "off"
      :hx-get (h/rpc #'select-search (update opts :html var-name))
      :name "search"
      :hx-target "#popup"
      :hx-trigger "open, keyupup change delay:300"
      :placeholder "search.."}]
    (chevron ["size-6"])]
   [:div#popup.popup {:class "hidden z-100 w-full p-1 min-h-40 max-h-60 overflow-y-auto shadow-md border border-gray-100 bg-white"}]
   (h/js "inputWithPopup('#select','#popup')")])


(defn table-item [tbl]
  [:div {:class "flex py-1"}
   [:input {:type "hidden" :name "table_name" :value (:table_name tbl)}]
   [:span {:class "text-gray-400"} (:table_schema tbl)]
   [:span {:class "text-gray-300"} "."]
   [:span (:table_name tbl)]])

(defn column-item [tbl]
  [:div {:class "flex space-x-1"}
   [:input {:name "table_name" :value (:table_name tbl)}]
   [:span {:class "text-gray-100"} (:table_name tbl)]
   [:span (:column_name tbl)]])

(defn form-result [context request opts]
  [:pre {:class "mt-4 bg-gray-100 rounded p-6"} (pr-str (slurp (:body request)))])

(defn index-html [context request]
  (let [today (java.time.LocalDate/now)]
    (h/layout
     context request
     {:content
      [:div
       [:form {:hx-post (h/rpc #'form-result) :hx-target "#form-result"}
        select-style
        (select context {:table "information_schema.tables" :column "table_name" :html #'table-item})

        [:br]
        ;; (select context {:table "information_schema.columns" :column "column_name" :html #'column-item})
        (h/btn {:type "submit"} "Submit")
        ]
       [:div#form-result]]})))


(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/select"       :fn #'index-html})

  )



(comment
  (def context mysystem/context)

  (mount-routes context)


 )
